package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.graphics.Bitmap;

import android.media.MediaPlayer;
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
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.UniversalPlayer;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.utils.UIHelper;


/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentPlayer extends Fragment implements MediaPlayer.OnPreparedListener {
    public static String TAG = "fragmentPlayer";

    private Button btnPlay;
    private RadioItem radioItem;
    private ImageView imgPlayerCover;
    private ImageView imgClosePlayer;
    private RelativeLayout rlTopSectionBckg;
    private UniversalPlayer universalPlayer;

    private View.OnClickListener btnPlayStopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (universalPlayer.isPrepaired) {
                universalPlayer.toggle();
                btnPlay.setText(universalPlayer.isPlaying() ? "Stop" : "Play");
            } else {
                runPlayer();
            }
        }
    };

    private View.OnClickListener imgClosePlayerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            universalPlayer.releasePlayer();
            getActivity().onBackPressed();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        btnPlay = (Button) view.findViewById(R.id.btnPlay);
        imgClosePlayer = (ImageView) view.findViewById(R.id.imgClosePlayer);
        imgClosePlayer.setOnClickListener(imgClosePlayerClickListener);
        btnPlay.setOnClickListener(btnPlayStopClickListener);
        rlTopSectionBckg = (RelativeLayout) view.findViewById(R.id.rlTopSectionBckg);
        imgPlayerCover = (ImageView) view.findViewById(R.id.imgPlayerCover);
        if (radioItem == null) {
            radioItem = (RadioItem) savedInstanceState.get(ActivityPlayer.RADIO_ITEM_EXTRA);
        }
        if (radioItem != null) {
            initRadioUI(view);
            universalPlayer = UniversalPlayer.getInstance();
            runPlayer();
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

    }

    public void setRadioItem(RadioItem radioItem) {
        this.radioItem = radioItem;
    }

    private void runPlayer() {
        universalPlayer.setPreparedListener(FragmentPlayer.this);
        if (universalPlayer.isPlaying() && universalPlayer.isCurrentMediaItem(radioItem)) {
            btnPlay.setText("Stop");
            return;
        } else if (universalPlayer.isPlaying()) {
            universalPlayer.resetPlayer();
        }
        universalPlayer.setMediaItem(radioItem);
        universalPlayer.prepare();
        btnPlay.setText("Fetching...");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        btnPlay.setText("Stop");
    }

    @Override
    public void onDestroy() {
        if (universalPlayer != null) {
            universalPlayer.setPreparedListener(null);
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ActivityPlayer.RADIO_ITEM_EXTRA, radioItem);
        super.onSaveInstanceState(outState);
    }
}
