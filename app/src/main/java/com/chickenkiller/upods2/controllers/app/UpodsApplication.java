package com.chickenkiller.upods2.controllers.app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.SystemClock;

import com.chickenkiller.upods2.controllers.database.SQLdatabaseManager;
import com.chickenkiller.upods2.controllers.internet.NetworkTasksService;
import com.chickenkiller.upods2.models.Category;
import com.chickenkiller.upods2.utils.Logger;
import com.facebook.FacebookSdk;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.concurrent.TimeUnit;

/**
 * Created by alonzilberman on 7/31/15.
 */
public class UpodsApplication extends Application {

    private static final int CHECK_NEW_EPISODS_INTENT_CODE = 2302;
    private static final String TAG = "UpodsApplication";
    private static Context applicationContext;
    private static SQLdatabaseManager databaseManager;

    @Override
    public void onCreate() {
        //LeakCanary.install(this);
        applicationContext = getApplicationContext();
        databaseManager = new SQLdatabaseManager(applicationContext);
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

        runMainService();
        setAlarmManagerTasks();
    }

    private void runMainService() {
        startService(new Intent(this, MainService.class));
    }

    public static void setAlarmManagerTasks() {
        AlarmManager alarmMgr = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(applicationContext, NetworkTasksService.class);
        intent.setAction(NetworkTasksService.ACTION_CHECK_FOR_NEW_EPISODS);
        PendingIntent alarmIntent = PendingIntent.getService(applicationContext, CHECK_NEW_EPISODS_INTENT_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (SettingsManager.getInstace().getBooleanSettingsValue(SettingsManager.JS_NOTIFY_EPISODS)) {
            long intervel = SettingsManager.getInstace().getIntSettingsValue(SettingsManager.JS_PODCASTS_UPDATE_TIME);
            intervel = TimeUnit.HOURS.toMillis(intervel);
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + intervel,
                    SystemClock.elapsedRealtime() + intervel, alarmIntent);
            Logger.printInfo(TAG, "Alarm managers - Episods check for updates task added");
        } else {
            alarmIntent.cancel();
            alarmMgr.cancel(alarmIntent);
            Logger.printInfo(TAG, "Alarm managers - Episods check for updates task canceled");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static Context getContext() {
        return applicationContext;
    }

    public static SQLdatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
