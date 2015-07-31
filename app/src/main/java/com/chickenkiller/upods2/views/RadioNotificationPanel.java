package com.chickenkiller.upods2.views;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.RadioItem;

/**
 * Created by alonzilberman on 7/31/15.
 */
public class RadioNotificationPanel extends PlayerNotificationPanel {
    private static int NOTIFICATION_ID = 12;


    public RadioNotificationPanel(Context mContext, RadioItem radioItem) {
        super(mContext, radioItem);
        nBuilder = new NotificationCompat.Builder(mContext);
        nBuilder.setContentTitle(radioItem.getName());
        nBuilder.setSmallIcon(R.drawable.ic_play_white_24dp);
        remoteView = new RemoteViews(mContext.getPackageName(), R.layout.player_notification);

        remoteView.setTextViewText(R.id.tvPlayerTitleNtBar, radioItem.getName());

        setListeners(remoteView);
        nBuilder.setContent(remoteView);

        nManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, nBuilder.build());
    }
}
