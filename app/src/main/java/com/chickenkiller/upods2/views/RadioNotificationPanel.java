package com.chickenkiller.upods2.views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityPlayer;
import com.chickenkiller.upods2.controllers.UniversalPlayer;
import com.chickenkiller.upods2.models.RadioItem;

/**
 * Created by alonzilberman on 7/31/15.
 */
public class RadioNotificationPanel extends PlayerNotificationPanel {
    private static int NOTIFICATION_ID = 12;

    public RadioNotificationPanel(Context mContext, RadioItem radioItem) {
        super(mContext, radioItem);
        this.nBuilder = new NotificationCompat.Builder(mContext);
        this.nBuilder.setContentTitle(radioItem.getName());
        this.nBuilder.setContentInfo(radioItem.getName());
        this.nBuilder.setContentText(radioItem.getName());
        this.nBuilder.setSmallIcon(R.drawable.ic_play_white_24dp);
        this.nBuilder.setAutoCancel(false);
        this.nBuilder.setOngoing(false);

        Intent intentOpen = new Intent(mContext, ActivityPlayer.class);
        intentOpen.putExtra(ActivityPlayer.RADIO_ITEM_EXTRA, UniversalPlayer.getInstance().getPlayingMediaItem());
        PendingIntent piOpen = PendingIntent.getActivity(mContext, 0, intentOpen, 0);

        this.nBuilder.setContentIntent(piOpen);

        this.remoteView = new RemoteViews(mContext.getPackageName(), R.layout.player_notification);
        this.remoteView.setTextViewText(R.id.tvPlayerTitleNtBar, radioItem.getName());

        setListeners();

        this.nBuilder.setContent(remoteView);
        Notification notification = nBuilder.build();

        this.nManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        this.nManager.notify(NOTIFICATION_ID, notification);

        Glide.with(mContext)
                .load(radioItem.getCoverImageUrl())
                .asBitmap()
                .into(new NotificationTarget(
                        mContext,
                        remoteView,
                        R.id.imgPlayerNtBar,
                        notification,
                        NOTIFICATION_ID));
    }

    @Override
    protected void setListeners() {
        //remoteView.setPendingIntentTemplate(R.id.rlNotification, piOpen);
        super.setListeners();
    }

    @Override
    public void updateNotificationStatus(Status status) {
        String text = status == Status.PLAYING ? "Stop" : "Play";
        remoteView.setTextViewText(R.id.btnPlayNtBar, text);
        nManager.notify(NOTIFICATION_ID, nBuilder.build());
        super.updateNotificationStatus(status);
    }
}
