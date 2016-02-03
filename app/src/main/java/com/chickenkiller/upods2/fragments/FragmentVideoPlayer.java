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

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.utils.Logger;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentVideoPlayer extends Fragment implements IVLCVout.Callback, LibVLC.HardwareAccelerationError {

    public static String TAG = "FragmentVideoPlayer";

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder mHolder;
    private IOperationFinishCallback onPlayingFailedCallback;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<FragmentVideoPlayer> mOwner;

        public MyPlayerListener(FragmentVideoPlayer owner) {
            mOwner = new WeakReference<FragmentVideoPlayer>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            FragmentVideoPlayer player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    Logger.printInfo(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_video_payer, container, false);
        ((IToolbarHolder) getActivity()).getToolbar().setVisibility(View.GONE);
        mSurface = (SurfaceView) view.findViewById(R.id.sfVideoPlayer);
        mHolder = mSurface.getHolder();
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onResume() {
        super.onResume();
        createPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }


    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if (mHolder == null || mSurface == null)
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
        mHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
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
            mHolder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
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
        mHolder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
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
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }
}
