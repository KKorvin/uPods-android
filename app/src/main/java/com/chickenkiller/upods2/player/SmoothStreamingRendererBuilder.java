/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chickenkiller.upods2.player;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Handler;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import com.google.android.exoplayer.chunk.MultiTrackChunkSource;
import com.google.android.exoplayer.chunk.VideoFormatSelectorUtil;
import com.google.android.exoplayer.drm.DrmSessionManager;
import com.google.android.exoplayer.drm.MediaDrmCallback;
import com.google.android.exoplayer.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.smoothstreaming.SmoothStreamingChunkSource;
import com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifest;
import com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifest.StreamElement;
import com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifestParser;
import com.google.android.exoplayer.text.TextTrackRenderer;
import com.google.android.exoplayer.text.ttml.TtmlParser;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.Util;

import java.io.IOException;
import java.util.Arrays;

/**
 * A {@link MediaPlayer.RendererBuilder} for SmoothStreaming.
 */
public class SmoothStreamingRendererBuilder implements MediaPlayer.RendererBuilder,
    ManifestFetcher.ManifestCallback<SmoothStreamingManifest> {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int VIDEO_BUFFER_SEGMENTS = 200;
  private static final int AUDIO_BUFFER_SEGMENTS = 60;
  private static final int TEXT_BUFFER_SEGMENTS = 2;
  private static final int LIVE_EDGE_LATENCY_MS = 30000;

  private final Context context;
  private final String userAgent;
  private final String url;
  private final MediaDrmCallback drmCallback;

  private MediaPlayer player;
  private MediaPlayer.RendererBuilderCallback callback;
  private ManifestFetcher<SmoothStreamingManifest> manifestFetcher;

  public SmoothStreamingRendererBuilder(Context context, String userAgent, String url,
      MediaDrmCallback drmCallback) {
    this.context = context;
    this.userAgent = userAgent;
    this.url = url;
    this.drmCallback = drmCallback;
  }

  @Override
  public void buildRenderers(MediaPlayer player, MediaPlayer.RendererBuilderCallback callback) {
    this.player = player;
    this.callback = callback;
    String manifestUrl = url;
    if (!manifestUrl.endsWith("/Manifest")) {
      manifestUrl += "/Manifest";
    }
    SmoothStreamingManifestParser parser = new SmoothStreamingManifestParser();
    manifestFetcher = new ManifestFetcher<>(manifestUrl,
        new DefaultHttpDataSource(userAgent, null), parser);
    manifestFetcher.singleLoad(player.getMainHandler().getLooper(), this);
  }

  @Override
  public void onSingleManifestError(IOException exception) {
    callback.onRenderersError(exception);
  }

  @Override
  public void onSingleManifest(SmoothStreamingManifest manifest) {
    Handler mainHandler = player.getMainHandler();
    LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, player);

    // Check drm support if necessary.
    DrmSessionManager drmSessionManager = null;
    if (manifest.protectionElement != null) {
      if (Util.SDK_INT < 18) {
        callback.onRenderersError(
            new UnsupportedDrmException(UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME));
        return;
      }
      try {
        drmSessionManager = new StreamingDrmSessionManager(manifest.protectionElement.uuid,
            player.getPlaybackLooper(), drmCallback, null, player.getMainHandler(), player);
      } catch (UnsupportedDrmException e) {
        callback.onRenderersError(e);
        return;
      }
    }

    // Obtain stream elements for playback.
    int audioStreamElementCount = 0;
    int textStreamElementCount = 0;
    int videoStreamElementIndex = -1;
    for (int i = 0; i < manifest.streamElements.length; i++) {
      if (manifest.streamElements[i].type == StreamElement.TYPE_AUDIO) {
        audioStreamElementCount++;
      } else if (manifest.streamElements[i].type == StreamElement.TYPE_TEXT) {
        textStreamElementCount++;
      } else if (videoStreamElementIndex == -1
          && manifest.streamElements[i].type == StreamElement.TYPE_VIDEO) {
        videoStreamElementIndex = i;
      }
    }

    // Determine which video tracks we should use for playback.
    int[] videoTrackIndices = null;
    if (videoStreamElementIndex != -1) {
      try {
        videoTrackIndices = VideoFormatSelectorUtil.selectVideoFormatsForDefaultDisplay(context,
            Arrays.asList(manifest.streamElements[videoStreamElementIndex].tracks), null, false);
      } catch (DecoderQueryException e) {
        callback.onRenderersError(e);
        return;
      }
    }

    // Build the video renderer.
    final MediaCodecVideoTrackRenderer videoRenderer;
    if (videoTrackIndices == null || videoTrackIndices.length == 0) {
      videoRenderer = null;
    } else {
      DataSource videoDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
      ChunkSource videoChunkSource = new SmoothStreamingChunkSource(manifestFetcher,
          videoStreamElementIndex, videoTrackIndices, videoDataSource,
          new AdaptiveEvaluator(bandwidthMeter), LIVE_EDGE_LATENCY_MS);
      ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource, loadControl,
          VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true, mainHandler, player,
          MediaPlayer.TYPE_VIDEO);
      videoRenderer = new MediaCodecVideoTrackRenderer(videoSampleSource, drmSessionManager, true,
          MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000, null, mainHandler, player, 50);
    }

    // Build the audio renderer.
    final String[] audioTrackNames;
    final MultiTrackChunkSource audioChunkSource;
    final MediaCodecAudioTrackRenderer audioRenderer;
    if (audioStreamElementCount == 0) {
      audioTrackNames = null;
      audioChunkSource = null;
      audioRenderer = null;
    } else {
      audioTrackNames = new String[audioStreamElementCount];
      ChunkSource[] audioChunkSources = new ChunkSource[audioStreamElementCount];
      DataSource audioDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
      FormatEvaluator audioFormatEvaluator = new FormatEvaluator.FixedEvaluator();
      audioStreamElementCount = 0;
      for (int i = 0; i < manifest.streamElements.length; i++) {
        if (manifest.streamElements[i].type == StreamElement.TYPE_AUDIO) {
          audioTrackNames[audioStreamElementCount] = manifest.streamElements[i].name;
          audioChunkSources[audioStreamElementCount] = new SmoothStreamingChunkSource(
              manifestFetcher, i, new int[] {0}, audioDataSource, audioFormatEvaluator,
              LIVE_EDGE_LATENCY_MS);
          audioStreamElementCount++;
        }
      }
      audioChunkSource = new MultiTrackChunkSource(audioChunkSources);
      ChunkSampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource, loadControl,
          AUDIO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true, mainHandler, player,
          MediaPlayer.TYPE_AUDIO);
      audioRenderer = new MediaCodecAudioTrackRenderer(audioSampleSource, drmSessionManager, true,
          mainHandler, player);
    }

    // Build the text renderer.
    final String[] textTrackNames;
    final MultiTrackChunkSource textChunkSource;
    final TrackRenderer textRenderer;
    if (textStreamElementCount == 0) {
      textTrackNames = null;
      textChunkSource = null;
      textRenderer = null;
    } else {
      textTrackNames = new String[textStreamElementCount];
      ChunkSource[] textChunkSources = new ChunkSource[textStreamElementCount];
      DataSource ttmlDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
      FormatEvaluator ttmlFormatEvaluator = new FormatEvaluator.FixedEvaluator();
      textStreamElementCount = 0;
      for (int i = 0; i < manifest.streamElements.length; i++) {
        if (manifest.streamElements[i].type == StreamElement.TYPE_TEXT) {
          textTrackNames[textStreamElementCount] = manifest.streamElements[i].language;
          textChunkSources[textStreamElementCount] = new SmoothStreamingChunkSource(manifestFetcher,
              i, new int[] {0}, ttmlDataSource, ttmlFormatEvaluator, LIVE_EDGE_LATENCY_MS);
          textStreamElementCount++;
        }
      }
      textChunkSource = new MultiTrackChunkSource(textChunkSources);
      ChunkSampleSource ttmlSampleSource = new ChunkSampleSource(textChunkSource, loadControl,
          TEXT_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true, mainHandler, player,
          MediaPlayer.TYPE_TEXT);
      textRenderer = new TextTrackRenderer(ttmlSampleSource, player, mainHandler.getLooper(),
          new TtmlParser());
    }

    // Invoke the callback.
    String[][] trackNames = new String[MediaPlayer.RENDERER_COUNT][];
    trackNames[MediaPlayer.TYPE_AUDIO] = audioTrackNames;
    trackNames[MediaPlayer.TYPE_TEXT] = textTrackNames;

    MultiTrackChunkSource[] multiTrackChunkSources =
        new MultiTrackChunkSource[MediaPlayer.RENDERER_COUNT];
    multiTrackChunkSources[MediaPlayer.TYPE_AUDIO] = audioChunkSource;
    multiTrackChunkSources[MediaPlayer.TYPE_TEXT] = textChunkSource;

    TrackRenderer[] renderers = new TrackRenderer[MediaPlayer.RENDERER_COUNT];
    renderers[MediaPlayer.TYPE_VIDEO] = videoRenderer;
    renderers[MediaPlayer.TYPE_AUDIO] = audioRenderer;
    renderers[MediaPlayer.TYPE_TEXT] = textRenderer;
    callback.onRenderers(trackNames, multiTrackChunkSources, renderers, bandwidthMeter);
  }

}
