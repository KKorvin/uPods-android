package com.chickenkiller.upods2.controllers.app;

import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;
import com.amazonaws.regions.Regions;
import com.chickenkiller.upods2.utils.Logger;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    private static SettingsManager settingsManager;

    public static final String JS_PODCASTS_UPDATE_TIME = "podcasts_update_time";
    public static final String JS_START_SCREEN = "start_screen";
    public static final String JS_NOTIFY_EPISODS = "notify_episods";

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
        readSettings(getDataset());
        saveSettings(getDataset());
    }

    private Dataset getDataset() {
        CognitoSyncManager syncClient = new CognitoSyncManager(
                UpodsApplication.getContext(),
                Regions.US_EAST_1,
                LoginMaster.getInstance().getCredentialsProvider());
        Dataset dataset = syncClient.openOrCreateDataset(AWS_DATASET_NAME);
        return dataset;
    }

    private void readSettings(Dataset dataset) {
        settingsObject = new JSONObject();
        try {
            if (dataset.get(JS_SETTINGS) != null) {
                settingsObject = new JSONObject(dataset.get(JS_SETTINGS));
            } else {
                settingsObject.put(JS_START_SCREEN, DEFAULT_START_SCREEN);
                settingsObject.put(JS_NOTIFY_EPISODS, DEFAULT_NOTIFY_EPISODS);
                settingsObject.put(JS_PODCASTS_UPDATE_TIME, DEFAULT_PODCAST_UPDATE_TIME);
            }
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't read settings from cognito");
            e.printStackTrace();
        }

    }

    private void saveSettings(Dataset dataset) {
        if (settingsObject != null) {
            dataset.put(JS_SETTINGS, settingsObject.toString());
            dataset.synchronize(new Dataset.SyncCallback() {
                @Override
                public void onSuccess(Dataset dataset, List<Record> updatedRecords) {

                }

                @Override
                public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {
                    List<Record> resolvedRecords = new ArrayList<Record>();
                    for (SyncConflict conflict : conflicts) {
                        resolvedRecords.add(conflict.resolveWithLastWriterWins());
                    }
                    dataset.resolve(resolvedRecords);
                    return true;
                }

                @Override
                public boolean onDatasetDeleted(Dataset dataset, String datasetName) {
                    return false;
                }

                @Override
                public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames) {
                    return true;
                }

                @Override
                public void onFailure(DataStorageException dse) {
                    Logger.printInfo(TAG, "Failed to save to cognito");
                    dse.printStackTrace();
                }
            });
        }
    }

    public void sync() {
        saveSettings(getDataset());
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

        String pUpdateTime = Prefs.getString(JS_PODCASTS_UPDATE_TIME, String.valueOf(DEFAULT_PODCAST_UPDATE_TIME));
        putSettingsValue(JS_PODCASTS_UPDATE_TIME, Integer.valueOf(pUpdateTime));
        saveSettings(getDataset());
    }
}
