package com.chickenkiller.upods2.controllers.app;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.chickenkiller.upods2.controllers.internet.NetworkTasksService;
import com.chickenkiller.upods2.models.Category;
import com.facebook.FacebookSdk;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by alonzilberman on 7/31/15.
 */
public class UpodsApplication extends Application {

    private static Context applicationContext;

    @Override
    public void onCreate() {
        applicationContext = getApplicationContext();
        FacebookSdk.sdkInitialize(applicationContext);
        LoginMaster.getInstance().init();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        SettingsManager.getInstace().init();
        Category.initPodcastsCatrgories();
        SimpleCacheManager.getInstance().removeExpiredCache();

        super.onCreate();

        runNetworkTasksService();
    }

    private void runNetworkTasksService() {
        Intent intent = new Intent(this, NetworkTasksService.class);
        intent.setAction(NetworkTasksService.ACTION_CHECK_FOR_NEW_EPISODS);
        startService(intent);
    }

    public static Context getContext() {
        return applicationContext;
    }
}
