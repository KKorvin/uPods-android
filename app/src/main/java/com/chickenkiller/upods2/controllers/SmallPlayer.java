package com.chickenkiller.upods2.controllers;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.models.RadioItem;

/**
 * Created by alonzilberman on 8/5/15.
 */
public class SmallPlayer implements IPlayerStateListener {

    private ImageView imgCover;
    private TextView tvTitle;
    private ImageButton btnPlay;
    private RelativeLayout rlSmallPLayer;

    private View.OnClickListener btnPlayOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            UniversalPlayer universalPlayer = UniversalPlayer.getInstance();
            universalPlayer.toggle();
            btnPlay.setBackgroundResource(universalPlayer.isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
        }
    };

    public SmallPlayer(View parentView) {
        this.rlSmallPLayer = (RelativeLayout) parentView.findViewById(R.id.rlSmallPlayer);
        if (this.rlSmallPLayer == null) {
            return;
        }
        if (UniversalPlayer.getInstance().isPrepaired) {
            this.imgCover = (ImageView) parentView.findViewById(R.id.imgSmallPlayerCover);
            this.tvTitle = (TextView) parentView.findViewById(R.id.tvSmallPlayerTitle);
            this.btnPlay = (ImageButton) parentView.findViewById(R.id.btnSmallPlayerPlay);
            this.btnPlay.setOnClickListener(btnPlayOnClickListener);
            this.btnPlay.setBackgroundResource(UniversalPlayer.getInstance().isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
            this.rlSmallPLayer.setVisibility(View.VISIBLE);
            if (UniversalPlayer.getInstance().getPlayingMediaItem() instanceof RadioItem) {
                RadioItem radioItem = (RadioItem) UniversalPlayer.getInstance().getPlayingMediaItem();
                this.tvTitle.setText(radioItem.getName());
                Glide.with(parentView.getContext()).load(radioItem.getCoverImageUrl()).crossFade().into(new GlideDrawableImageViewTarget(imgCover));
            }
            UniversalPlayer.getInstance().setPlayerStateListener(this);
        } else {
            this.rlSmallPLayer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStateChanged(UniversalPlayer.State state) {
        btnPlay.setBackgroundResource(state == UniversalPlayer.State.PLAYING ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
    }

    public void destroy() {
        UniversalPlayer.getInstance().setPlayerStateListener(null);
    }
}
