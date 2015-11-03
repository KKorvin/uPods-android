package com.chickenkiller.upods2.controllers.player;

import android.media.MediaPlayer;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;
import com.chickenkiller.upods2.views.DefaultNotificationPanel;
import com.chickenkiller.upods2.views.PlayerNotificationPanel;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alonzilberman on 7/29/15.
 */
public class UniversalPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {


    private static final long RECONNECT_RATE = 5000;

    public enum State {PLAYING, PAUSED}

    public static final String INTENT_ACTION_PLAY = "com.chickenkiller.upods2.player.PLAY";
    public static final String INTENT_ACTION_PAUSE = "com.chickenkiller.upods2.player.PAUSE";
    private static final String PLAYER_LOG = "UniversalPlayer";

    public static UniversalPlayer universalPlayer;
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnPreparedListener preparedListener;
    private IPlayerStateListener playerStateListener;
    private IPlayableMediaItem mediaItem;
    private PlayerNotificationPanel notificationPanel;

    private TimerTask autoReconector;

    public boolean isPrepaired;


    private UniversalPlayer() {
    }

    public static UniversalPlayer getInstance() {
        if (universalPlayer == null) {
            universalPlayer = new UniversalPlayer();
        }
        return universalPlayer;
    }

    /**
     * Checks if given type of media item is supported, if yes copies it and save instance in player.
     *
     * @param mediaItem
     */
    public void setMediaItem(IPlayableMediaItem mediaItem) {
        if (isCurrentMediaItem(mediaItem)) {
            return;
        }
        if (mediaItem instanceof RadioItem) {
            this.mediaItem = new RadioItem((RadioItem) mediaItem);
        } else if (mediaItem instanceof Podcast) {
            this.mediaItem = new Podcast((Podcast) mediaItem);
        } else {
            throw new RuntimeException("Unsupported type of MediaItem");
        }
    }

    public void setPreparedListener(MediaPlayer.OnPreparedListener preparedListener) {
        this.preparedListener = preparedListener;
    }

    public void setPlayerStateListener(IPlayerStateListener playerStateListener) {
        this.playerStateListener = playerStateListener;
    }

    public void prepare(IPlayableMediaItem mediaItem, MediaPlayer.OnPreparedListener preparedListener) {
        setPreparedListener(preparedListener);
        setMediaItem(mediaItem);
        prepare();
    }

    /**
     * Main prepare method. Prepares media item to be played.
     */
    public void prepare() {
        if (mediaItem == null) {
            throw new RuntimeException("MediaItem is not set. Call setMediaItem before prepare.");
        }
        if (!isPrepaired) {
            try {
                Logger.printInfo(PLAYER_LOG, "Trying to play: " + mediaItem.getAudeoLink());
                if (!menageAudeoFormats()) {
                    return;
                }
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                mediaPlayer.setDataSource(mediaItem.getAudeoLink());
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnErrorListener(this);
                mediaPlayer.setOnInfoListener(this);
                mediaPlayer.setOnBufferingUpdateListener(this);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call it to manage different media formats, will try to convert format to mp3 if it is not supported
     *
     * @return true - if prepare should continues, false if it will call atomatycly bac callback
     */
    private boolean menageAudeoFormats() {
        if (mediaItem.getAudeoLink().matches("(.+\\.m3u$)|(.+\\.pls$)")) {
            MediaUtils.extractMp3FromFile(mediaItem.getAudeoLink(), new IOperationFinishWithDataCallback() {
                @Override
                public void operationFinished(Object data) {
                    mediaItem.setAudeoLink((String) data);
                    prepare();
                }
            });
            return false;
        }
        return true;
    }

    public void start() {
        if (mediaPlayer != null && isPrepaired) {
            mediaPlayer.start();
            if (notificationPanel != null) {
                notificationPanel.updateNotificationStatus(State.PLAYING);
            }
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PLAYING);
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null && isPrepaired && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (notificationPanel != null) {
                notificationPanel.updateNotificationStatus(State.PAUSED);
            }
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PAUSED);
            }
        }
    }

    public void toggle() {
        if (mediaPlayer.isPlaying()) {
            pause();
        } else {
            start();
        }

    }

    public boolean isPlaying() {
        if (mediaPlayer != null && isPrepaired) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }


    /**
     * Restarts player in the soft way (didn't release all resorce and callbacks)
     */
    public void softRestart() {
        if (mediaPlayer != null) {
            resetPlayer();
            prepare();
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            mediaItem = null;
            preparedListener = null;
            playerStateListener = null;
            isPrepaired = false;
        }
        if (notificationPanel != null) {
            notificationPanel.notificationCancel();
        }
    }

    public void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            isPrepaired = false;
        }
        if (notificationPanel != null) {
            notificationPanel.notificationCancel();
        }
    }

    public void removeListeners() {
        preparedListener = null;
        playerStateListener = null;
    }

    public IPlayableMediaItem getPlayingMediaItem() {
        return mediaItem;
    }


    public boolean isCurrentMediaItem(IPlayableMediaItem mediaItem) {
        if (this.mediaItem == null) {
            return false;
        }

        if (this.mediaItem.getAudeoLink().equals(mediaItem.getAudeoLink())) {
            return this.mediaItem.getAudeoLink().equals(mediaItem.getAudeoLink());
        }
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPrepaired = true;
        mediaPlayer.start();
        if (mediaItem instanceof RadioItem) {
            if (notificationPanel != null) {
                notificationPanel.notificationCancel();
            }
            notificationPanel = new DefaultNotificationPanel(UpodsApplication.getContext(), (RadioItem) mediaItem);
            ProfileManager.getInstance().addRecentMediaItem(mediaItem);
        }
        if (preparedListener != null) {
            preparedListener.onPrepared(mediaPlayer);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logger.printInfo(PLAYER_LOG, "Error code: " + String.valueOf(what));
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Logger.printInfo(PLAYER_LOG, "Info code: " + String.valueOf(what));
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START && !GlobalUtils.isInternetConnected()) {
            runReconnectTask();
        }
        return false;
    }

    private void runReconnectTask() {
        if (autoReconector == null) {
            autoReconector = new TimerTask() {
                @Override
                public void run() {
                    if (GlobalUtils.isInternetConnected()) {
                        Logger.printInfo(PLAYER_LOG, "Reconnector -> got internet connection -> restarting player...");
                        softRestart();
                        autoReconector.cancel();
                        autoReconector = null;
                    } else {
                        Logger.printInfo(PLAYER_LOG, "Reconnector -> no internet connection -> will try again later...");
                    }
                }
            };
        }
        new Timer().scheduleAtFixedRate(autoReconector, 0, RECONNECT_RATE);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Log.i(PLAYER_LOG, "Player buffer: " + String.valueOf(percent));
    }
}
