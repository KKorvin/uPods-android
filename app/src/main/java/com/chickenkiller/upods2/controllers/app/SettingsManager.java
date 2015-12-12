package com.chickenkiller.upods2.controllers.app;

import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;
import com.amazonaws.regions.Regions;
import com.chickenkiller.upods2.utils.Logger;

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

    private static SettingsManager settingsManager;

    public static final String JS_PODCASTS_UPDATE_TIME = "podcasts_update_time";

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
            saveSettings(getDataset());
        } catch (JSONException e) {
            Logger.printInfo(TAG, "Can't put value for key: " + key + " to json settings");
            e.printStackTrace();
        }
    }
}
