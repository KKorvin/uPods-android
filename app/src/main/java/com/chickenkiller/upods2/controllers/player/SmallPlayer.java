package com.chickenkiller.upods2.controllers.player;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.fragments.FragmentPlayer;
import com.chickenkiller.upods2.interfaces.IOnPositionUpdatedCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.interfaces.ITrackable;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.MediaUtils;

/**
 * Created by alonzilberman on 8/5/15.
 */
public class SmallPlayer implements IPlayerStateListener, View.OnClickListener {

    private ImageView imgCover;
    private TextView tvTitle;
    private TextView tvSubTtitle;
    private SeekBar sbSmallPlayer;
    private ImageButton btnPlay;
    private RelativeLayout rlSmallPLayer;
    private Activity mActivity;

    private PlayerPositionUpdater playerPositionUpdater;

    private long maxDuration = -1;

    private View.OnClickListener btnPlayOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            UniversalPlayer universalPlayer = UniversalPlayer.getInstance();
            universalPlayer.toggle();
            btnPlay.setBackgroundResource(universalPlayer.isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
        }
    };

    public SmallPlayer(View parentView, Activity mActivity) {
        this.mActivity = mActivity;
        this.rlSmallPLayer = (RelativeLayout) parentView.findViewById(R.id.rlSmallPlayer);
        if (this.rlSmallPLayer == null) {
            return;
        }
        rlSmallPLayer.setOnClickListener(this);
        if (UniversalPlayer.getInstance().isPrepaired) {
            this.imgCover = (ImageView) parentView.findViewById(R.id.imgSmallPlayerCover);
            this.tvTitle = (TextView) parentView.findViewById(R.id.tvSmallPlayerTitle);
            this.tvSubTtitle = (TextView) parentView.findViewById(R.id.tvSmallPlayerSubTtile);
            this.btnPlay = (ImageButton) parentView.findViewById(R.id.btnSmallPlayerPlay);
            this.btnPlay.setOnClickListener(btnPlayOnClickListener);
            this.btnPlay.setBackgroundResource(UniversalPlayer.getInstance().isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
            this.rlSmallPLayer.setVisibility(View.VISIBLE);
            this.sbSmallPlayer = (SeekBar) parentView.findViewById(R.id.sbSmallPlayer);
            IPlayableMediaItem playingMediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
            if (playingMediaItem instanceof ITrackable) {
                tvTitle.setText(((ITrackable) playingMediaItem).getSelectedTrack().getTitle());
            } else {
                tvTitle.setText(playingMediaItem.getName());
            }
            this.tvSubTtitle.setText(playingMediaItem.getSubHeader());
            Glide.with(parentView.getContext()).load(playingMediaItem.getCoverImageUrl()).crossFade().into(new GlideDrawableImageViewTarget(imgCover));
            UniversalPlayer.getInstance().setPlayerStateListener(this);
            runPositionUpdater();
        } else {
            this.rlSmallPLayer.setVisibility(View.GONE);
        }
    }

    private void runPositionUpdater() {
        playerPositionUpdater = (PlayerPositionUpdater) new PlayerPositionUpdater(new IOnPositionUpdatedCallback() {
            @Override
            public void poistionUpdated(int currentPoistion) {
                if (maxDuration < 0) {
                    IPlayableMediaItem playingMediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
                    if (playingMediaItem instanceof Podcast) {
                        Track track = ((Podcast) playingMediaItem).getSelectedTrack();
                        maxDuration = MediaUtils.timeStringToLong(track.getDuration());
                    }
                    maxDuration = maxDuration > 0 ? maxDuration : FragmentPlayer.DEFAULT_RADIO_DURATIO;
                }
                int progress = (int) (currentPoistion * 100 / maxDuration);
                sbSmallPlayer.setProgress(progress);
            }

            @Override
            public void poistionUpdaterStoped() {

            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onStateChanged(UniversalPlayer.State state) {
        btnPlay.setBackgroundResource(state == UniversalPlayer.State.PLAYING ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
    }

    public void destroy() {
        if (playerPositionUpdater != null) {
            playerPositionUpdater.cancel(false);
        }
        mActivity = null;
    }

    @Override
    public void onClick(View view) {
        Intent intentOpen = new Intent(mActivity, ActivityPlayer.class);
        mActivity.startActivity(intentOpen);
        mActivity.finish();
    }
}
