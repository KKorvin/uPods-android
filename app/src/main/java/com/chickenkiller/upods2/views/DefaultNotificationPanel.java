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
import com.chickenkiller.upods2.controllers.app.MainBroadcastRecivier;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;

/**
 * Created by alonzilberman on 7/31/15.
 */
public class DefaultNotificationPanel extends PlayerNotificationPanel {
    private static int NOTIFICATION_ID = 12;
    private PendingIntent playIntent;
    private PendingIntent pauseIntent;

    public DefaultNotificationPanel(Context mContext, IPlayableMediaItem playableMediaItem) {
        super(mContext, playableMediaItem);
        this.nBuilder = new NotificationCompat.Builder(mContext);
        this.nBuilder.setContentTitle(playableMediaItem.getName());
        this.nBuilder.setContentInfo(playableMediaItem.getName());
        this.nBuilder.setContentText(playableMediaItem.getName());
        this.nBuilder.setSmallIcon(R.drawable.ic_play_white_24dp);
        this.nBuilder.setAutoCancel(false);
        this.nBuilder.setOngoing(true);

        Intent intentOpen = new Intent(mContext, ActivityPlayer.class);
        PendingIntent piOpen = PendingIntent.getActivity(mContext, 0, intentOpen, 0);

        this.nBuilder.setContentIntent(piOpen);
        this.nBuilder.setOngoing(false);

        this.remoteView = new RemoteViews(mContext.getPackageName(), R.layout.player_notification);
        this.remoteView.setTextViewText(R.id.tvPlayerTitleNtBar, playableMediaItem.getName());
        this.remoteView.setTextViewText(R.id.tvPlayerSubTitleNtBar, playableMediaItem.getSubHeader());
        setListeners();

        this.nBuilder.setContent(remoteView);
        Notification notification = nBuilder.build();
        this.nManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        this.nManager.notify(NOTIFICATION_ID, notification);

        Glide.with(mContext)
                .load(playableMediaItem.getCoverImageUrl())
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
        Intent plIntent = new Intent(mContext, MainBroadcastRecivier.class);
        plIntent.setAction(UniversalPlayer.INTENT_ACTION_PLAY);
        playIntent = PendingIntent.getBroadcast(mContext, 0, plIntent, 0);

        Intent pIntent = new Intent(mContext, MainBroadcastRecivier.class);
        pIntent.setAction(UniversalPlayer.INTENT_ACTION_PAUSE);
        pauseIntent = PendingIntent.getBroadcast(mContext, 0, pIntent, 0);

        remoteView.setOnClickPendingIntent(R.id.btnPlayNtBar, pauseIntent);
        super.setListeners();
    }

    @Override
    public void updateNotificationStatus(UniversalPlayer.State state) {
        if (state == UniversalPlayer.State.PLAYING) {
            remoteView.setImageViewResource(R.id.btnPlayNtBar, R.drawable.ic_pause_white);
            nBuilder.setOngoing(true);
        } else {
            remoteView.setImageViewResource(R.id.btnPlayNtBar, R.drawable.ic_play_white);
            nBuilder.setOngoing(false);
        }
        remoteView.setOnClickPendingIntent(R.id.btnPlayNtBar, state == UniversalPlayer.State.PLAYING ? pauseIntent : playIntent);
        nBuilder.setSmallIcon(state == UniversalPlayer.State.PLAYING ? R.drawable.ic_play_white_24dp : R.drawable.ic_pause_white);
        nManager.notify(NOTIFICATION_ID, nBuilder.build());
        super.updateNotificationStatus(state);
    }
}
