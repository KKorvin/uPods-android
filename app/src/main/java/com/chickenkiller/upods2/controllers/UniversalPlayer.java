package com.chickenkiller.upods2.controllers;

import android.media.MediaPlayer;

import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayerStateListener;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.views.PlayerNotificationPanel;
import com.chickenkiller.upods2.views.RadioNotificationPanel;

/**
 * Created by alonzilberman on 7/29/15.
 */
public class UniversalPlayer implements MediaPlayer.OnPreparedListener {

    public enum State {PLAYING, PAUSED}

    public static final String INTENT_ACTION_PLAY = "com.chickenkiller.upods2.player.PLAY";
    public static final String INTENT_ACTION_PAUSE = "com.chickenkiller.upods2.player.PAUSE";

    public static UniversalPlayer universalPlayer;
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnPreparedListener preparedListener;
    private IPlayerStateListener playerStateListener;
    private IPlayableMediaItem mediaItem;
    private PlayerNotificationPanel notificationPanel;

    public boolean isPrepaired;


    private UniversalPlayer() {
    }

    public static UniversalPlayer getInstance() {
        if (universalPlayer == null) {
            universalPlayer = new UniversalPlayer();
        }
        return universalPlayer;
    }

    public void setMediaItem(IPlayableMediaItem mediaItem) {
        if (isCurrentMediaItem(mediaItem)) {
            return;
        }
        if (mediaItem instanceof RadioItem) {
            this.mediaItem = new RadioItem((RadioItem) mediaItem);
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

    public void prepare() {
        if (mediaItem == null) {
            throw new RuntimeException("MediaItem is not set. Call setMediaItem before prepare.");
        }
        if (!isPrepaired) {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                if (mediaItem instanceof RadioItem) {
                    mediaPlayer.setDataSource(((RadioItem) mediaItem).getStreamUrl());
                }
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if (mediaPlayer != null && isPrepaired) {
            mediaPlayer.start();
            notificationPanel.updateNotificationStatus(State.PLAYING);
            if (playerStateListener != null) {
                playerStateListener.onStateChanged(State.PLAYING);
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null && isPrepaired && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notificationPanel.updateNotificationStatus(State.PAUSED);
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
        if (this.mediaItem.equals(mediaItem)) {
            return this.mediaItem.getStreamUrl().equals(mediaItem.getStreamUrl());
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
            notificationPanel = new RadioNotificationPanel(UpodsApplication.getContext(), (RadioItem) mediaItem);
        }
        if (preparedListener != null) {
            preparedListener.onPrepared(mediaPlayer);
        }
    }
}
