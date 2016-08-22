package com.chickenkiller.upods2.controllers.internet;

import android.os.AsyncTask;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ServerApi;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Alon Zilberman on 1/2/16.
 */
public class SyncMaster extends AsyncTask<Void, JSONObject, Void> {

    public enum Task {PULL, PUSH, SYNC}

    public static final String TYPE_FB = "facebook";
    public static final String TYPE_TWITTER = "twitter";
    public static final String TYPE_VK = "vkontakte";
    public static final String TYPE_GLOBAL = "global";

    public static final String GLOBAL_TOKEN = "global_token";
    public static boolean isRunning;

    private IOperationFinishCallback profileSyncedCallback;
    private String type;
    private String token;
    private String secret;
    private boolean profilePulled;

    private Task task;


    public SyncMaster(String type, String token, String secret, Task task) {
        this.type = type;
        this.token = token;
        this.secret = secret;
        this.profilePulled = false;
        this.task = task;
        isRunning = true;
    }

    public void setProfileSyncedCallback(IOperationFinishCallback profileSyncedCallback) {
        this.profileSyncedCallback = profileSyncedCallback;
    }

    private void printMessage(JSONObject result) throws JSONException {
        if (result.has("result") && result.getJSONObject("result").has("message")) {
            Logger.printInfo("SyncMaster", result.getJSONObject("result").getString("message"));
        } else if (result.has("message")) {
            Logger.printInfo("SyncMaster", result.getString("message"));
        }
    }

    private void pullUser(String link) throws Exception {

        //Start getting user (pull)
        RequestBody pullFormBody = new FormBody.Builder().
                add("secret", secret).
                add("token", token).
                add("type", type).
                add("task", "get_user").build();
        Request requestGetUser = new Request.Builder().url(link).post(pullFormBody).build();

        JSONObject getUserResult = BackendManager.getInstance().sendSynchronicRequest(requestGetUser);

        printMessage(getUserResult);

        if (getUserResult.has("result")) {
            if (getUserResult.getJSONObject("result").has("global_token")) {
                String globalToken = getUserResult.getJSONObject("result").getString("global_token");
                Prefs.putString(GLOBAL_TOKEN, globalToken);
                token = globalToken;
                type = TYPE_GLOBAL;
            }
            if (getUserResult.getJSONObject("result").has("profile")) {
                JSONObject profile = new JSONObject(getUserResult.getJSONObject("result").getString("profile"));
                if (task == Task.PULL) {
                    publishProgress(profile);
                }
            }
            if (getUserResult.getJSONObject("result").has("settings")) {
                JSONObject settings = new JSONObject(getUserResult.getJSONObject("result").getString("settings"));
                SettingsManager.getInstace().readSettings(settings);
                SettingsManager.getInstace().saveSettings(settings);
            }
        }
        profilePulled = true;
        Prefs.putString(SettingsManager.PREFS_LAST_CLOUD_SYNC, GlobalUtils.getCurrentDateTimeUS());
        //End getting user
    }

    private void pushUser(String link) throws Exception {
        JSONObject jProfile = ProfileManager.getInstance().getAsJson();
        RequestBody pushFormBody = new FormBody.Builder().
                add("token", token).
                add("secret", secret).
                add("type", type).
                add("task", "sync").
                add("settings", SettingsManager.getInstace().getAsJson().toString()).
                add("profile", jProfile.toString()).build();
        Request requestSyncUser = new Request.Builder().url(link).post(pushFormBody).build();
        JSONObject syncUserResult = BackendManager.getInstance().sendSynchronicRequest(requestSyncUser);

        if (!type.equals(TYPE_GLOBAL) && syncUserResult.has("global_token")) {
            String token = syncUserResult.getString("global_token");
            Prefs.putString(GLOBAL_TOKEN, token);
        }
        printMessage(syncUserResult);
    }

    @Override
    protected Void doInBackground(Void... params) {
        StringBuilder link = new StringBuilder();
        link.append(ServerApi.USER_SYNC);

        try {
            if (task == Task.PULL || !Prefs.contains(GLOBAL_TOKEN)) {
                pullUser(link.toString());
            } else {
                profilePulled = true;
            }

            if (task == Task.PUSH) {
                while (!profilePulled) {
                    Thread.sleep(200);
                }
                pushUser(link.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(JSONObject... profile) {
        ProfileManager.getInstance().readFromJson(profile[0]);
        profilePulled = true;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (profileSyncedCallback != null) {
            profileSyncedCallback.operationFinished();
        }
        isRunning = false;
    }
}
