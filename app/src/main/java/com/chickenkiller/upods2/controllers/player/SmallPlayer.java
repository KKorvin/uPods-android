package com.chickenkiller.upods2.controllers.player;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
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
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.interfaces.ITrackable;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.MediaUtils;
import com.chickenkiller.upods2.utils.ui.LetterBitmap;
import com.chickenkiller.upods2.utils.ui.UIHelper;

/**
 * Created by alonzilberman on 8/5/15.
 */
public class SmallPlayer implements IPlayerStateListener, View.OnClickListener, MediaPlayer.OnPreparedListener {

    private static final int COVER_IMAGE_SIZE = UIHelper.dpToPixels(64);

    private ImageView imgCover;
    private TextView tvTitle;
    private TextView tvSubTtitle;
    private SeekBar sbSmallPlayer;
    private ImageButton btnPlay;
    private RelativeLayout rlSmallPLayer;
    private Activity mActivity;
    private UniversalPlayer universalPlayer;

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
        this.universalPlayer = UniversalPlayer.getInstance();
        this.rlSmallPLayer = (RelativeLayout) parentView.findViewById(R.id.rlSmallPlayer);
        if (this.rlSmallPLayer != null) {
            this.imgCover = (ImageView) parentView.findViewById(R.id.imgSmallPlayerCover);
            this.tvTitle = (TextView) parentView.findViewById(R.id.tvSmallPlayerTitle);
            this.tvSubTtitle = (TextView) parentView.findViewById(R.id.tvSmallPlayerSubTtile);
            this.btnPlay = (ImageButton) parentView.findViewById(R.id.btnSmallPlayerPlay);
            this.btnPlay.setOnClickListener(btnPlayOnClickListener);
            this.sbSmallPlayer = (SeekBar) parentView.findViewById(R.id.sbSmallPlayer);
        }
    }

    private void initPlayerUI() {
        if (this.rlSmallPLayer == null) {
            return;
        }
        rlSmallPLayer.setOnClickListener(this);
        if (universalPlayer.isPrepaired) {
            this.rlSmallPLayer.setVisibility(View.VISIBLE);
            this.btnPlay.setBackgroundResource(universalPlayer.isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_white);
            IPlayableMediaItem playingMediaItem = universalPlayer.getPlayingMediaItem();
            if (playingMediaItem instanceof ITrackable) {
                tvTitle.setText(((ITrackable) playingMediaItem).getSelectedTrack().getTitle());
            } else {
                tvTitle.setText(playingMediaItem.getName());
            }
            this.tvSubTtitle.setText(playingMediaItem.getSubHeader());
            if (playingMediaItem.getCoverImageUrl() == null) {
                final LetterBitmap letterBitmap = new LetterBitmap(mActivity);
                Bitmap letterTile = letterBitmap.getLetterTile(playingMediaItem.getName(), playingMediaItem.getName(), COVER_IMAGE_SIZE, COVER_IMAGE_SIZE);
                imgCover.setImageBitmap(letterTile);
            } else {
                Glide.with(mActivity).load(playingMediaItem.getCoverImageUrl()).crossFade().into(new GlideDrawableImageViewTarget(imgCover));
            }
        } else {
            this.rlSmallPLayer.setVisibility(View.GONE);
        }
    }

    private void setPlayerCallbacks() {
        universalPlayer.setPreparedListener(this);
        universalPlayer.setPlayerStateListener(this);
        universalPlayer.setOnAutonomicTrackChangeCallback(new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                initPlayerUI();
            }
        });
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
        mActivity = null;
    }

    public void onPause() {
        if (playerPositionUpdater != null) {
            playerPositionUpdater.cancel(false);
        }
        UniversalPlayer.getInstance().removeListeners();
    }

    public void onResume() {
        initPlayerUI();
        setPlayerCallbacks();
        runPositionUpdater();
    }

    @Override
    public void onClick(View view) {
        Intent intentOpen = new Intent(mActivity, ActivityPlayer.class);
        mActivity.startActivity(intentOpen);
        mActivity.overridePendingTransition( R.anim.slide_in_bottom, R.anim.slide_out_bottom);
        mActivity.finish();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        initPlayerUI();
    }
}
