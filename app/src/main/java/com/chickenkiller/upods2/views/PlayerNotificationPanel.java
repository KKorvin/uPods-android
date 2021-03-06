package com.chickenkiller.upods2.views;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.models.MediaItem;

/**
 * Created by Alon Zilberman on 7/31/15.
 */
public abstract class PlayerNotificationPanel {


    protected static int NOTIFICATION_ID;

    protected Context mContext;
    protected NotificationManager nManager;
    protected NotificationCompat.Builder nBuilder;
    protected RemoteViews remoteView;
    protected MediaItem mediaItem;

    protected UniversalPlayer.State currentState;

    public PlayerNotificationPanel(Context mContext, MediaItem mediaItem) {
        this.mContext = mContext;
        this.mediaItem = mediaItem;
    }

    protected void setListeners() {

    }

    public void notificationCancel() {
        nManager.cancel(NOTIFICATION_ID);
    }

    public void updateNotificationStatus(UniversalPlayer.State state) {
        currentState = state;
    }

    public UniversalPlayer.State getCurrentState() {
        return currentState;
    }
}
