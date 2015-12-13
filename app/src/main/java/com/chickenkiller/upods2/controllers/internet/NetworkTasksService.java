package com.chickenkiller.upods2.controllers.internet;

import android.app.IntentService;
import android.content.Intent;

import com.chickenkiller.upods2.utils.Logger;

/**
 * Created by alonzilberman on 12/13/15.
 */
public class NetworkTasksService extends IntentService {

    private final static String TAG = "NetworkTasksService";

    public NetworkTasksService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.printInfo(TAG, "I am starting to run by intent: " + intent.getAction());
    }
}


