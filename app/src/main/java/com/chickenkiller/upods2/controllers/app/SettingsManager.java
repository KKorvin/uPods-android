package com.chickenkiller.upods2.controllers.app;

import com.chickenkiller.upods2.controllers.internet.SyncMaster;
import com.chickenkiller.upods2.utils.Logger;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alonzilberman on 12/12/15.
 */
public class SettingsManager {

    private static final String TAG = "SettingsManager";
    private static final String AWS_DATASET_NAME = "preferences";
    private static final String JS_SETTINGS = "settings";


    private static final int DEFAULT_PODCAST_UPDATE_TIME = 24; //hours
    private static final boolean DEFAULT_NOTIFY_EPISODS = true;
    private static final String DEFAULT_START_SCREEN = "rs_featured";
    private static final String DEFAULT_STREAM_QUALITY = "hight";

    private static SettingsManager settingsManager;

    public static final String JS_PODCASTS_UPDATE_TIME = "podcasts_update_time";
    public static final String JS_START_SCREEN = "start_screen";
    public static final String JS_NOTIFY_EPISODS = "notify_episods";
    public static final String JS_STREAM_QUALITY = "stream_quality";

    private JSONObject settingsObject;

    private SettingsManager() {

    }

    public static SettingsManager getInstace() {
        if (settingsManager == null) {
            settingsManager = new SettingsManager();
        }
        return settingsManager;
    }

    public void init() {
        readSettings();
        saveSettings();
    }

    public void readSettings(JSONObject jsonObject) {
        settingsObject = jsonObject;
        readSettings();
    }

    private void readSettings() {
        try {
            if (Prefs.getString(JS_SETTINGS, null) != null) {
                settingsObject = new JSONObject(Prefs.getString(JS_SETTINGS, null));
            } else {
                settingsObject = new JSONObject();
                settingsObject.put(JS_START_SCREEN, DEFAULT_START_SCREEN);
                settingsObject.put(JS_NOTIFY_EPISODS, DEFAULT_NOTIFY_EPISODS);
                settingsObject.put(JS_PODCASTS_UPDATE_TIME, DEFAULT_PODCAST_UPDATE_TIME);
                settingsObject.put(JS_STREAM_QUALITY, DEFAULT_STREAM_QUALITY);
            }
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read settings from cognito");
            e.printStackTrace();
        }

    }

    private void saveSettings() {
        if (settingsObject != null) {
            Prefs.putString(JS_SETTINGS, settingsObject.toString());
        }
        if (LoginMaster.getInstance().isLogedIn()) {
            SyncMaster.saveToCloud();
        }
    }

    public void sync() {
        saveSettings();
    }

    public int getIntSettingsValue(String key) {
        try {
            return settingsObject.getInt(key);
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read value with key: " + key + " from json settings");
            e.printStackTrace();
            return -1;
        }
    }

    public boolean getBooleanSettingsValue(String key) {
        try {
            return settingsObject.getBoolean(key);
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read value with key: " + key + " from json settings");
            e.printStackTrace();
            return false;
        }
    }

    public String getStringSettingValue(String key) {
        try {
            return settingsObject.getString(key);
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read value with key: " + key + " from json settings");
            e.printStackTrace();
            return "";
        }
    }

    public void putSettingsValue(String key, Object value) {
        try {
            if (value instanceof Integer) {
                settingsObject.put(key, (int) value);
            } else if (value instanceof Boolean) {
                settingsObject.put(key, (boolean) value);
            } else if (value instanceof String) {
                settingsObject.put(key, (String) value);
            }
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't put value for key: " + key + " to json settings");
            e.printStackTrace();
        }
    }

    public void initSettingsFromPreferences() {
        putSettingsValue(JS_NOTIFY_EPISODS, Prefs.getBoolean(JS_NOTIFY_EPISODS, DEFAULT_NOTIFY_EPISODS));
        putSettingsValue(JS_START_SCREEN, Prefs.getString(JS_START_SCREEN, DEFAULT_START_SCREEN));
        putSettingsValue(JS_STREAM_QUALITY, Prefs.getString(JS_STREAM_QUALITY, DEFAULT_STREAM_QUALITY));

        String pUpdateTime = Prefs.getString(JS_PODCASTS_UPDATE_TIME, String.valueOf(DEFAULT_PODCAST_UPDATE_TIME));
        putSettingsValue(JS_PODCASTS_UPDATE_TIME, Integer.valueOf(pUpdateTime));
        saveSettings();
    }

    public JSONObject getAsJson() {
        return settingsObject;
    }

}
