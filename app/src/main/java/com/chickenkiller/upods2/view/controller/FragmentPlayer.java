package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.player.ExtractorRendererBuilder;
import com.chickenkiller.upods2.player.MediaPlayer;
import com.chickenkiller.upods2.utils.UIHelper;
import com.google.android.exoplayer.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer.extractor.mp4.FragmentedMp4Extractor;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.extractor.ts.AdtsExtractor;
import com.google.android.exoplayer.extractor.webm.WebmExtractor;
import com.google.android.exoplayer.util.Util;

/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentPlayer extends Fragment {
    public static final int TYPE_DASH = 0;
    public static final int TYPE_SS = 1;
    public static final int TYPE_HLS = 2;
    public static final int TYPE_MP4 = 3;
    public static final int TYPE_MP3 = 4;
    public static final int TYPE_FMP4 = 5;
    public static final int TYPE_WEBM = 6;
    public static final int TYPE_MKV = 7;
    public static final int TYPE_TS = 8;
    public static final int TYPE_AAC = 9;
    public static final int TYPE_M4A = 10;
    public static String TAG = "fragmentPlayer";

    private Button btnPlay;
    private RadioItem radioItem;
    private ImageView imgPlayerCover;
    private RelativeLayout rlTopSectionBckg;
    private int contentType;
    private MediaPlayer player;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        btnPlay = (Button) view.findViewById(R.id.btnPlay);
        rlTopSectionBckg = (RelativeLayout) view.findViewById(R.id.rlTopSectionBckg);
        imgPlayerCover = (ImageView) view.findViewById(R.id.imgPlayerCover);
        if (radioItem != null) {
            initRadioUI(view);
        }
        return view;
    }

    public void initRadioUI(View view) {
        Glide.with(getActivity()).load(radioItem.getCoverImageUrl()).centerCrop().crossFade().into(new GlideDrawableImageViewTarget(imgPlayerCover) {
            @Override
            public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                super.onResourceReady(drawable, anim);
                Bitmap bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
                int dominantColor = UIHelper.getDominantColor(bitmap);
                rlTopSectionBckg.setBackgroundColor(dominantColor);
            }
        });
        contentType = TYPE_MP3;
        player = new MediaPlayer(getRendererBuilder());
        player.prepare();
        player.setPlayWhenReady(true);
    }

    public void setRadioItem(RadioItem radioItem) {
        this.radioItem = radioItem;
    }

    private MediaPlayer.RendererBuilder getRendererBuilder() {
        Uri contentUri = Uri.parse(radioItem.getStreamUrl());
        String userAgent = Util.getUserAgent(getActivity(), "ExoPlayerDemo");
        switch (contentType) {
            // case TYPE_SS:
            //     return new SmoothStreamingRendererBuilder(getActivity(), userAgent, contentUri.toString(),
            //             new SmoothStreamingTestMediaDrmCallback());
            //case TYPE_DASH:
            //    return new DashRendererBuilder(getActivity(), userAgent, contentUri.toString(),
            //            new WidevineTestMediaDrmCallback(contentId), audioCapabilities);
            //case TYPE_HLS:
            //    return new HlsRendererBuilder(getActivity(), userAgent, contentUri.toString(), audioCapabilities);
            // case TYPE_M4A: // There are no file format differences between M4A and MP4.
            case TYPE_MP4:
                return (MediaPlayer.RendererBuilder) new ExtractorRendererBuilder(getActivity(), userAgent, contentUri, new Mp4Extractor());
            case TYPE_MP3:
                return (MediaPlayer.RendererBuilder) new ExtractorRendererBuilder(getActivity(), userAgent, contentUri, new Mp3Extractor());
            // case TYPE_TS:
            //return new ExtractorRendererBuilder(getActivity(), userAgent, contentUri,
            //         new TsExtractor(0, audioCapabilities));
            case TYPE_AAC:
                return (MediaPlayer.RendererBuilder) new ExtractorRendererBuilder(getActivity(), userAgent, contentUri, new AdtsExtractor());
            case TYPE_FMP4:
                return (MediaPlayer.RendererBuilder) new ExtractorRendererBuilder(getActivity(), userAgent, contentUri,
                        new FragmentedMp4Extractor());
            case TYPE_WEBM:
            case TYPE_MKV:
                return (MediaPlayer.RendererBuilder) new ExtractorRendererBuilder(getActivity(), userAgent, contentUri, new WebmExtractor());
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }
}
