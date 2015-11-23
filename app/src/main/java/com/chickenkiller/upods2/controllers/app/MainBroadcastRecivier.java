package com.chickenkiller.upods2.controllers.app;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chickenkiller.upods2.controllers.internet.DownloadMaster;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.enums.Direction;

/**
 * Created by alonzilberman on 7/31/15.
 */
public class MainBroadcastRecivier extends BroadcastReceiver {

    public static final String TAG = "MainBroadcastRecivier";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.printInfo(TAG, intent.getAction());
        if (intent.getAction().equals(UniversalPlayer.INTENT_ACTION_PLAY)) {
            if (UniversalPlayer.getInstance().isPrepaired) {
                UniversalPlayer.getInstance().start();
            }
        } else if (intent.getAction().equals(UniversalPlayer.INTENT_ACTION_PAUSE)) {
            if (UniversalPlayer.getInstance().isPrepaired) {
                UniversalPlayer.getInstance().pause();
            }
        } else if (intent.getAction().equals(UniversalPlayer.INTENT_ACTION_BACKWARD)) {
            if (UniversalPlayer.getInstance().isPrepaired) {
                UniversalPlayer.getInstance().changeTrackToDirection(Direction.LEFT);
            }
        } else if (intent.getAction().equals(UniversalPlayer.INTENT_ACTION_FORWARD)) {
            if (UniversalPlayer.getInstance().isPrepaired) {
                UniversalPlayer.getInstance().changeTrackToDirection(Direction.RIGHT);
            }
        } else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            DownloadMaster.getInstance().markDownloadTaskFinished(downloadId);
        }
    }
}
