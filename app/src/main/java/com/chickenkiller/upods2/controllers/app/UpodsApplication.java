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
import com.yandex.metrica.YandexMetrica;


/**
 * Created by Alon Zilberman on 7/31/15.
 */
public class UpodsApplication extends Application {

    private static final int CHECK_NEW_EPISODS_INTENT_CODE = 2302;

    private static final String TAG = "UpodsApplication";
    private static Context applicationContext;
    private static SQLdatabaseManager databaseManager;
    private static boolean isLoaded;

    @Override
    public void onCreate() {
        isLoaded = false;
        applicationContext = getApplicationContext();
        YandexMetrica.activate(getApplicationContext(), Config.YANDEX_METRICS_API_KEY);
        YandexMetrica.enableActivityAutoTracking(this);
        FacebookSdk.sdkInitialize(applicationContext);
        LoginMaster.getInstance().init();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true).build();

        super.onCreate();
    }

    /**
     * All resources which can be loaded not in onCreate(), should be load here,
     * this function called while splash screen is active
     */
    public static void initAllResources() {
        if (!isLoaded) {
            databaseManager = new SQLdatabaseManager(applicationContext);
            SettingsManager.getInstace().init();
            Category.initPodcastsCatrgories();
            SimpleCacheManager.getInstance().removeExpiredCache();
            runMainService();
            setAlarmManagerTasks();
            isLoaded = true;
        }
    }

    private static void runMainService() {
        applicationContext.startService(new Intent(applicationContext, MainService.class));
    }

    public static void setAlarmManagerTasks() {
        setAlarmManagerTasks(applicationContext);
    }

    public static void setAlarmManagerTasks(Context context) {
        try {
            if (ProfileManager.getInstance().getSubscribedPodcasts().size() == 0) {
                return;
            }
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NetworkTasksService.class);
            intent.setAction(NetworkTasksService.ACTION_CHECK_FOR_NEW_EPISODS);
            PendingIntent alarmIntent = PendingIntent.getService(context, CHECK_NEW_EPISODS_INTENT_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            if (SettingsManager.getInstace().getBooleanSettingsValue(SettingsManager.JS_NOTIFY_EPISODES)) {
                long interval = SettingsManager.getInstace().getIntSettingsValue(SettingsManager.JS_PODCASTS_UPDATE_TIME);
                //interval = TimeUnit.MINUTES.toMillis(60);
                alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(),
                        interval, alarmIntent);
                Logger.printInfo(TAG, "Alarm managers - Episods check for updates task added");
            } else {
                alarmIntent.cancel();
                alarmMgr.cancel(alarmIntent);
                Logger.printInfo(TAG, "Alarm managers - Episods check for updates task canceled");
            }
        } catch (Exception e) {
            Logger.printInfo(TAG, "Alarm managers - can't set alarm manager");
            e.printStackTrace();
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
