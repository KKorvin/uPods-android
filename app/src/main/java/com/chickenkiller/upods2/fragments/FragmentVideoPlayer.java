package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.controllers.player.VideoPlayerPositionUpdater;
import com.chickenkiller.upods2.interfaces.IOnPositionUpdatedCallback;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;


/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentVideoPlayer extends Fragment implements IVLCVout.Callback, LibVLC.HardwareAccelerationError, MediaPlayer.EventListener {

    public static String TAG = "FragmentVideoPlayer";

    // display surface
    private SurfaceView sfVideo;
    private SurfaceHolder shVideoHolder;
    private IOperationFinishCallback onPlayingFailedCallback;
    private boolean isVideoPlayerReady = false;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean isChangingProgress;
    private long maxDuration = -1;
    private VideoPlayerPositionUpdater videoPlayerPositionUpdater;

    //Views
    private ImageButton btnPlay;
    private ProgressBar pbLoading;
    private RelativeLayout rlVideoControls;
    private TextView tvVideoDuration;
    private TextView tvVideoCurrent;
    private SeekBar sbPlayerProgress;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_video_payer, container, false);
        ((IToolbarHolder) getActivity()).getToolbar().setVisibility(View.GONE);
        sfVideo = (SurfaceView) view.findViewById(R.id.sfVideoPlayer);
        btnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);
        rlVideoControls = (RelativeLayout) view.findViewById(R.id.rlPlayerContorls);
        tvVideoCurrent = (TextView) view.findViewById(R.id.tvVideoCurrent);
        tvVideoDuration = (TextView) view.findViewById(R.id.tvVideoDuration);
        sbPlayerProgress = (SeekBar) view.findViewById(R.id.sbPlayerProgress);
        setVideoHolder();

        MediaItem mediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
        if (mediaItem instanceof Podcast) {
            tvVideoDuration.setText(((Podcast) mediaItem).getSelectedTrack().getDuration());
        }
        setListeners();
        return view;
    }

    private void setVideoHolder() {
        if (shVideoHolder == null && sfVideo != null) {
            shVideoHolder = sfVideo.getHolder();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setVideoHolder();
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pbLoading != null) {
            pbLoading.setVisibility(View.VISIBLE);
        }
        setVideoHolder();
        createPlayer();
        setVideoPlayerPositionUpdater();
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
        if (videoPlayerPositionUpdater != null) {
            videoPlayerPositionUpdater.cancel(true);
            videoPlayerPositionUpdater = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void setVideoPlayerPositionUpdater() {
        if (videoPlayerPositionUpdater == null) {
            videoPlayerPositionUpdater = new VideoPlayerPositionUpdater(new IOnPositionUpdatedCallback() {
                @Override
                public void poistionUpdated(int currentPoistion) {
                    tvVideoCurrent.setText(MediaUtils.formatMsToTimeString(currentPoistion));
                    if (!isChangingProgress) {
                        if (maxDuration < 0) {
                            maxDuration = MediaUtils.timeStringToLong(tvVideoDuration.getText().toString());
                        }
                        int progress = (int) (currentPoistion * 100 / maxDuration);
                        sbPlayerProgress.setProgress(progress);
                    }
                }

                @Override
                public void poistionUpdaterStoped() {

                }
            }, mMediaPlayer);
            videoPlayerPositionUpdater.execute();
        }
    }

    private void setListeners() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    btnPlay.setImageResource(R.drawable.ic_play_white);
                } else {
                    mMediaPlayer.play();
                    btnPlay.setImageResource(R.drawable.ic_pause_white);
                }
            }
        });

        sfVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pbLoading.getVisibility() == View.GONE) {
                    rlVideoControls.setVisibility(rlVideoControls.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    btnPlay.setVisibility(btnPlay.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                }
            }
        });

        sbPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChangingProgress = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isChangingProgress = false;
                int progress = seekBar.getProgress();
                int position = (int) ((maxDuration * progress) / 100);
                seekBar.setProgress(progress);
                mMediaPlayer.setTime(position);
            }
        });
    }

    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if (shVideoHolder == null || sfVideo == null)
            return;

        // get screen size
        int w = getActivity().getWindow().getDecorView().getWidth();
        int h = getActivity().getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        shVideoHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) sfVideo.getLayoutParams();
        lp.width = w;
        lp.height = h;
        sfVideo.setLayoutParams(lp);
        sfVideo.invalidate();
    }

    private void createPlayer() {
        releasePlayer();
        String videoLink = UniversalPlayer.getInstance().getPlayingMediaItem().getAudeoLink();
        try {
            Logger.printInfo(TAG, "Trying to play video: " + videoLink);

            ArrayList<String> options = new ArrayList<String>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libvlc = new LibVLC(options);
            libvlc.setOnHardwareAccelerationError(this);
            shVideoHolder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(this);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(sfVideo);
            vout.addCallback(this);
            vout.attachViews();

            Media m = URLUtil.isValidUrl(videoLink) ? new Media(libvlc, Uri.parse(videoLink)) : new Media(libvlc, videoLink);
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
            Logger.printInfo(TAG, "Error creating video player: ");
            e.printStackTrace();
            if (onPlayingFailedCallback != null) {
                onPlayingFailedCallback.operationFinished();
            }
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        shVideoHolder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
        isVideoPlayerReady = false;

        if (rlVideoControls != null && btnPlay != null) {
            rlVideoControls.setVisibility(View.GONE);
            btnPlay.setVisibility(View.GONE);
        }
    }

    public void setOnPlayingFailedCallback(IOperationFinishCallback onPlayingFailedCallback) {
        this.onPlayingFailedCallback = onPlayingFailedCallback;
    }

    @Override
    public void eventHardwareAccelerationError() {
        // Handle errors with hardware acceleration
        Logger.printError(TAG, "Error with hardware acceleration");
        this.releasePlayer();
        if (onPlayingFailedCallback != null) {
            onPlayingFailedCallback.operationFinished();
        }
    }

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.EndReached:
                releasePlayer();
                getActivity().onBackPressed();
                break;
            case MediaPlayer.Event.Playing: {
                if (!isVideoPlayerReady) {
                    isVideoPlayerReady = true;
                    pbLoading.setVisibility(View.GONE);
                    String lastPosition = SettingsManager.getInstace().getPareSettingValue(SettingsManager.JS_EPISODS_POSITIONS,
                            ((Podcast) UniversalPlayer.getInstance().getPlayingMediaItem()).getSelectedTrack().getTitle());
                    if (!lastPosition.isEmpty()) {
                        mMediaPlayer.setTime(Integer.valueOf(lastPosition));
                    }
                }
            }
            case MediaPlayer.Event.Paused:
            case MediaPlayer.Event.Stopped:
            default:
                break;
        }
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }
}
