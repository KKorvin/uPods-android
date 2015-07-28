package com.chickenkiller.upods2.controllers;

import android.content.Context;

import com.google.android.exoplayer.ExoPlayer;

/**
 * Created by alonzilberman on 7/28/15.
 */
public class MediaPlayer {

    private static final int RENDERER_COUNT = 2;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int VIDEO_BUFFER_SEGMENTS = 200;
    private static final int AUDIO_BUFFER_SEGMENTS = 60;
    private static MediaPlayer player;

    private ExoPlayer exoPlayer;

    private MediaPlayer() {

    }

    public static MediaPlayer getInstance() {
        if (player == null) {
            player = new MediaPlayer();
        }
        return player;
    }

    private void initPlayer(Context mContext){
        /*exoPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT);
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        DataSourcece dataSource = new DefaultUriDataSource(mContext, null, "");
        Mp4Extractor extractor = new Mp4Extractor();
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(
                uri, dataSource, extractor, allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(
                sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);*/
    }
}
