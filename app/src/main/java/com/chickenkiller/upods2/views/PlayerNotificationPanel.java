package com.chickenkiller.upods2.views;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;

/**
 * Created by alonzilberman on 7/31/15.
 */
public abstract class PlayerNotificationPanel {


    protected static int NOTIFICATION_ID;

    protected Context mContext;
    protected NotificationManager nManager;
    protected NotificationCompat.Builder nBuilder;
    protected RemoteViews remoteView;
    protected IPlayableMediaItem mediaItem;

    public PlayerNotificationPanel(Context mContext, IPlayableMediaItem mediaItem) {
        this.mContext = mContext;
        this.mediaItem = mediaItem;
    }

    protected void setListeners() {

    }

    public void notificationCancel() {
        nManager.cancel(NOTIFICATION_ID);
    }

    public void updateNotificationStatus(UniversalPlayer.State state) {

    }

}
