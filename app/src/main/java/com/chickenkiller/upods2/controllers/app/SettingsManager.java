package com.chickenkiller.upods2.controllers.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.utils.Logger;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by alonzilberman on 12/12/15.
 */
public class SettingsManager {

    private static final String TAG = "SettingsManager";
    private static final String JS_SETTINGS = "settings";


    private static final int DEFAULT_PODCAST_UPDATE_TIME = 24; //hours
    private static final boolean DEFAULT_NOTIFY_EPISODS = true;
    private static final String DEFAULT_START_SCREEN = "rs_featured";

    private static SettingsManager settingsManager;

    public static final String TOPS_RU_LANGUAGE = "ru";
    public static final String TOPS_ENG_LANGUAGE = "en";

    public static final String PREFS_LAST_CLOUD_SYNC = "last_sync";

    public static final String JS_PODCASTS_UPDATE_TIME = "podcasts_update_time";
    public static final String JS_START_SCREEN = "start_screen";
    public static final String JS_NOTIFY_EPISODES = "notify_episods";
    public static final String JS_STREAM_QUALITY = "stream_quality";
    public static final String JS_TOPS_LANGUAGE = "tops_lng";

    public static final String DEFAULT_STREAM_QUALITY = "high";

    private SettingsManager() {
        super();
    }

    public static synchronized SettingsManager getInstace() {
        if (settingsManager == null) {
            settingsManager = new SettingsManager();
        }
        return settingsManager;
    }

    public void init() {
        readSettings(); //Call it here in order to create empty settings if not exist
    }

    public void readSettings(JSONObject jsonObject) {
        Prefs.putString(JS_SETTINGS, jsonObject.toString());
        readSettings();
    }

    private JSONObject readSettings() {
        try {
            if (Prefs.getString(JS_SETTINGS, null) == null) {
                JSONObject settingsObject = new JSONObject();
                settingsObject.put(JS_START_SCREEN, DEFAULT_START_SCREEN);
                settingsObject.put(JS_NOTIFY_EPISODES, DEFAULT_NOTIFY_EPISODS);
                settingsObject.put(JS_PODCASTS_UPDATE_TIME, DEFAULT_PODCAST_UPDATE_TIME);
                settingsObject.put(JS_STREAM_QUALITY, DEFAULT_STREAM_QUALITY);
                settingsObject.put(JS_TOPS_LANGUAGE, Locale.getDefault().getLanguage());

                Prefs.putString(JS_TOPS_LANGUAGE, Locale.getDefault().getLanguage());
                Prefs.putString(JS_SETTINGS, settingsObject.toString());
            }
            return new JSONObject(Prefs.getString(JS_SETTINGS, null));
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read settings");
            e.printStackTrace();
        }
        return null;
    }

    public int getIntSettingsValue(String key) {
        try {
            return readSettings().getInt(key);
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read value with key: " + key + " from json settings");
            e.printStackTrace();
            return -1;
        }
    }

    public boolean getBooleanSettingsValue(String key) {
        try {
            return readSettings().getBoolean(key);
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read value with key: " + key + " from json settings");
            e.printStackTrace();
            return false;
        }
    }

    public String getStringSettingValue(String key) {
        try {
            return readSettings().getString(key);
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read value with key: " + key + " from json settings");
            e.printStackTrace();
            return "";
        }
    }

    public String getPareSettingValue(String settingsKey, String pareKey) {
        try {
            JSONObject rootObject = readSettings();
            if (rootObject.has(settingsKey)) {
                JSONArray streamsArray = readSettings().getJSONArray(settingsKey);
                for (int i = 0; i < streamsArray.length(); i++) {
                    if (streamsArray.getJSONObject(i).has(pareKey)) {
                        return streamsArray.getJSONObject(i).getString(pareKey);
                    }
                }
            }
            return "";
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read value with key: " + settingsKey + " from json settings");
            e.printStackTrace();
            return "";
        }
    }


    public void saveStreamQualitySelection(MediaItem mediaItem, String quality) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("media_item_name", mediaItem.getName());
        values.put("quality", quality);
        database.replace("streams_quality", null, values);
    }

    public String getStreamQuality(MediaItem mediaItem) {
        String streamQuality = null;
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
        String args[] = {mediaItem.getName()};
        Cursor cursor = database.rawQuery("SELECT quality FROM streams_quality WHERE media_item_name = ?", args);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            streamQuality = cursor.getString(cursor.getColumnIndex("quality"));
        }
        return streamQuality;
    }

    /**
     * Automatically saves settings
     *
     * @param key
     * @param value
     */
    public void putSettingsValue(String key, Object value) {
        try {
            JSONObject settingsObject = readSettings();
            if (value instanceof Integer) {
                settingsObject.put(key, (int) value);
            } else if (value instanceof Boolean) {
                settingsObject.put(key, (boolean) value);
            } else if (value instanceof String) {
                settingsObject.put(key, value);
            } else if (value instanceof Pair) {
                String pairKey = (String) ((Pair) value).first;
                String pairValue = (String) ((Pair) value).second;
                JSONObject streamForItem = new JSONObject();
                JSONArray streamsArray = settingsObject.has(key) ? settingsObject.getJSONArray(key) : new JSONArray();

                JSONArray newStreamsArray = new JSONArray();
                streamForItem.put(pairKey, pairValue);
                for (int i = 0; i < streamsArray.length(); i++) {
                    if (!streamsArray.getJSONObject(i).has(pairKey)) {
                        newStreamsArray.put(streamsArray.getJSONObject(i));
                    }
                }
                newStreamsArray.put(streamForItem);
                settingsObject.put(key, newStreamsArray);
            }
            saveSettings(settingsObject);
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't put value for key: " + key + " to json settings");
            e.printStackTrace();
        }
    }


    public void initSettingsFromPreferences() {
        putSettingsValue(JS_NOTIFY_EPISODES, Prefs.getBoolean(JS_NOTIFY_EPISODES, DEFAULT_NOTIFY_EPISODS));
        putSettingsValue(JS_START_SCREEN, Prefs.getString(JS_START_SCREEN, DEFAULT_START_SCREEN));
        putSettingsValue(JS_STREAM_QUALITY, Prefs.getString(JS_STREAM_QUALITY, DEFAULT_STREAM_QUALITY));
        putSettingsValue(JS_TOPS_LANGUAGE, Prefs.getString(JS_TOPS_LANGUAGE, Locale.getDefault().getLanguage()));

        String pUpdateTime = Prefs.getString(JS_PODCASTS_UPDATE_TIME, String.valueOf(DEFAULT_PODCAST_UPDATE_TIME));
        putSettingsValue(JS_PODCASTS_UPDATE_TIME, Integer.valueOf(pUpdateTime));
    }

    public JSONObject getAsJson() {
        return readSettings();
    }


    public void saveSettings(JSONObject settingsObject) {
        if (settingsObject != null) {
            Prefs.putString(JS_SETTINGS, settingsObject.toString());
        }
    }

}
