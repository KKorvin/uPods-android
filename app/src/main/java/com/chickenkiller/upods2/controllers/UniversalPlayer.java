package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by alonzilberman on 7/29/15.
 */
public class UniversalPlayer implements MediaPlayer.OnPreparedListener {

    public static UniversalPlayer universalPlayer;
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnPreparedListener preparedListener;

    public boolean isPrepaired;


    private UniversalPlayer() {
    }

    public static UniversalPlayer getInstance() {
        if (universalPlayer == null) {
            universalPlayer = new UniversalPlayer();
        }
        return universalPlayer;
    }

    public void setPreparedListener(MediaPlayer.OnPreparedListener preparedListener) {
        this.preparedListener = preparedListener;
    }

    public void prepare(Context context, String url) {
        isPrepaired = false;
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
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

    public void release() {
        mediaPlayer.release();
        preparedListener = null;
        isPrepaired = false;
        universalPlayer = null;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.isPrepaired = true;
        if (this.preparedListener != null) {
            this.preparedListener.onPrepared(mediaPlayer);
        }
    }
}
