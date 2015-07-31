package com.chickenkiller.upods2.controllers;

import android.app.Application;
import android.content.Context;

/**
 * Created by alonzilberman on 7/31/15.
 */
public class UpodsApplication extends Application {

    private static Context applicationContext;

    @Override
    public void onCreate() {
        applicationContext = getApplicationContext();
        super.onCreate();
    }

    public static Context getContext() {
        return applicationContext;
    }
}
