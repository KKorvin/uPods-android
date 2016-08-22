package com.chickenkiller.upods2.controllers.app;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Alon Zilberman on 1/18/16.
 */
public class MainService extends Service {

    private IBinder mBinder;


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setIBinder(IBinder mBinder) {
        this.mBinder = mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
        super.onTaskRemoved(rootIntent);
    }
}
