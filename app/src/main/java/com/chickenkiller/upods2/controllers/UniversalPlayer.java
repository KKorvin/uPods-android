package com.chickenkiller.upods2.controllers;

import android.media.MediaPlayer;

import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.RadioItem;

/**
 * Created by alonzilberman on 7/29/15.
 */
public class UniversalPlayer implements MediaPlayer.OnPreparedListener {

    public static UniversalPlayer universalPlayer;
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnPreparedListener preparedListener;
    private MediaItem mediaItem;

    public boolean isPrepaired;


    private UniversalPlayer() {
    }

    public static UniversalPlayer getInstance() {
        if (universalPlayer == null) {
            universalPlayer = new UniversalPlayer();
        }
        return universalPlayer;
    }

    public void setMediaItem(MediaItem mediaItem) {
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

    public void prepare(MediaItem mediaItem, MediaPlayer.OnPreparedListener preparedListener) {
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
        }
    }

    public void toggle() {
        if (mediaPlayer != null && isPrepaired) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer != null && isPrepaired) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public void pause() {
        if (mediaPlayer != null && isPrepaired && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            mediaItem = null;
            isPrepaired = false;
        }
    }

    public void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            isPrepaired = false;
        }
    }

    public MediaItem getPlayingMediaItem() {
        return mediaItem;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.isPrepaired = true;
        this.mediaPlayer.start();
        if (this.preparedListener != null) {
            this.preparedListener.onPrepared(mediaPlayer);
        }
    }

    public boolean isCurrentMediaItem(MediaItem mediaItem) {
        if (this.mediaItem == null) {
            return false;
        }
        if (this.mediaItem instanceof RadioItem && mediaItem instanceof RadioItem) {
            return ((RadioItem) this.mediaItem).getStreamUrl().equals(((RadioItem) mediaItem).getStreamUrl());
        }
        return false;
    }
}
