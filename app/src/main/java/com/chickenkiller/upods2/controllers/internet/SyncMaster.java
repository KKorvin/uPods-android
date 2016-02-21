package com.chickenkiller.upods2.controllers.internet;

import android.os.AsyncTask;

import com.chickenkiller.upods2.controllers.app.LoginMaster;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ServerApi;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by alonzilberman on 1/2/16.
 */
public class SyncMaster extends AsyncTask<Void, Void, Void> {

    public static final String TYPE_FB = "facebook";
    public static final String TYPE_TWITTER = "twitter";
    public static final String TYPE_VK = "vkontakte";
    public static final String TYPE_GLOBAL = "global";

    public static final int TASK_GET_USER = 1;
    public static final int TASK_SYNC = 2;
    public static final String GLOBAL_TOKEN = "global_token";

    private IOperationFinishWithDataCallback profileSyncedCallback;
    private JSONObject result;
    private String type;
    private String token;
    private String secret;
    private int task;


    public SyncMaster(String type, String token, String secret, int task) {
        this.type = type;
        this.token = token;
        this.task = task;
        this.secret = secret;
    }

    public void setProfileSyncedCallback(IOperationFinishWithDataCallback profileSyncedCallback) {
        this.profileSyncedCallback = profileSyncedCallback;
    }

    @Override
    protected Void doInBackground(Void... params) {
        StringBuilder link = new StringBuilder();
        link.append(ServerApi.USER_SYNC);

        try {
            Request request = null;
            if (task == TASK_SYNC) {
                JSONObject jProfile = ProfileManager.getInstance().getAsJson();
                //Logger.printInfo("jProfile", jProfile.toString());
                RequestBody formBody = new FormBody.Builder().
                        add("token", token).
                        add("secret", secret).
                        add("type", type).
                        add("task", "sync").
                        add("settings", SettingsManager.getInstace().getAsJson().toString()).
                        add("profile", jProfile.toString()).build();
                request = new Request.Builder().url(link.toString()).post(formBody).build();
            } else {
                RequestBody formBody = new FormBody.Builder().
                        add("secret", secret).
                        add("token", token).
                        add("type", type).
                        add("task", "get_user").build();
                request = new Request.Builder().url(link.toString()).post(formBody).build();
            }
            result = BackendManager.getInstance().sendSynchronicRequest(request);

            if (task == TASK_SYNC) {
                if (!type.equals(TYPE_GLOBAL) && result.has("global_token")) {
                    String token = result.getString("global_token");
                    Prefs.putString(GLOBAL_TOKEN, token);
                }
            } else {
                if (result.has("result") && result.getJSONObject("result").has("global_token")) {
                    String token = result.getJSONObject("result").getString("global_token");
                    Prefs.putString(GLOBAL_TOKEN, token);
                }
            }
            if (result.has("result") && result.getJSONObject("result").has("message")) {
                Logger.printInfo("SyncMaster", result.getJSONObject("result").getString("message"));
            } else if (result.has("message")) {
                Logger.printInfo("SyncMaster", result.getString("message"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (profileSyncedCallback != null) {
            profileSyncedCallback.operationFinished(result);
        }
    }

    public static void saveToCloud() {
        LoginMaster loginMaster = LoginMaster.getInstance();
        SyncMaster profileSyncMaster = new SyncMaster(loginMaster.getLoginType(),
                loginMaster.getToken(), loginMaster.getSecret(), SyncMaster.TASK_SYNC);
        profileSyncMaster.execute();
    }
}
