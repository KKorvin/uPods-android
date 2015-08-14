package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.UniversalPlayer;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.utils.UIHelper;


/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentPlayer extends Fragment implements MediaPlayer.OnPreparedListener, IPlayerStateListener {
    public static String TAG = "fragmentPlayer";

    private ImageButton btnPlay;
    private RadioItem radioItem;
    private ImageView imgPlayerCover;
    private ImageView imgClosePlayer;
    private RelativeLayout rlTopSectionBckg;
    private UniversalPlayer universalPlayer;
    private TextView tvPlayserSubtitle;
    private TextView tvPlayerTitle;

    private View.OnClickListener btnPlayStopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (universalPlayer.isPrepaired) {
                universalPlayer.toggle();
                btnPlay.setBackgroundResource(universalPlayer.isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
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
        btnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        imgClosePlayer = (ImageView) view.findViewById(R.id.imgClosePlayer);
        imgClosePlayer.setOnClickListener(imgClosePlayerClickListener);
        btnPlay.setOnClickListener(btnPlayStopClickListener);
        rlTopSectionBckg = (RelativeLayout) view.findViewById(R.id.rlTopSectionBckg);
        imgPlayerCover = (ImageView) view.findViewById(R.id.imgPlayerCover);
        tvPlayerTitle = (TextView) view.findViewById(R.id.tvPlayerTitle);
        tvPlayserSubtitle = (TextView) view.findViewById(R.id.tvPlayserSubtitle);

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
        tvPlayerTitle.setText(radioItem.getName());
        tvPlayserSubtitle.setText(radioItem.getCountry());
        Glide.with(getActivity()).load(radioItem.getCoverImageUrl()).crossFade().into(new GlideDrawableImageViewTarget(imgPlayerCover) {
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
        universalPlayer.setPreparedListener(this);
        universalPlayer.setPlayerStateListener(this);
        if (universalPlayer.isPlaying() && universalPlayer.isCurrentMediaItem(radioItem)) {
            btnPlay.setBackgroundResource(R.drawable.ic_pause_white);
            return;
        } else if (universalPlayer.isPlaying()) {
            universalPlayer.resetPlayer();
        }
        universalPlayer.setMediaItem(radioItem);
        universalPlayer.prepare();
        btnPlay.setBackgroundResource(R.drawable.ic_play_white);
        //btnPlay.setText("Fetching...");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        btnPlay.setBackgroundResource(R.drawable.ic_pause_white);
    }

    @Override
    public void onDestroy() {
        if (universalPlayer != null) {
            universalPlayer.removeListeners();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ActivityPlayer.RADIO_ITEM_EXTRA, radioItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStateChanged(UniversalPlayer.State state) {
        btnPlay.setBackgroundResource(state == UniversalPlayer.State.PLAYING ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
    }
}
