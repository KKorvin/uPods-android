package com.chickenkiller.upods2.controllers.app;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

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
        Category.initPodcastsCatrgories();
        SimpleCacheManager.getInstance().removeExpiredCache();
        super.onCreate();
    }

    public static Context getContext() {
        return applicationContext;
    }
}
