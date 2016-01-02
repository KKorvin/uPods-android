package com.chickenkiller.upods2.controllers.internet;

import android.os.AsyncTask;

import com.chickenkiller.upods2.controllers.app.LoginMaster;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ServerApi;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;

/**
 * Created by alonzilberman on 1/2/16.
 */
public class SyncMaster extends AsyncTask<Void, Void, Void> {

    public static final String TYPE_FB = "facebook";
    public static final String TYPE_TWITTER = "twitter";
    public static final String TYPE_VK = "vkontakte";

    public static final int TASK_GET_USER = 1;
    public static final int TASK_SYNC = 2;

    private IOperationFinishWithDataCallback profileSyncedCallback;
    private JSONObject result;
    private String type;
    private String token;
    private int task;

    public SyncMaster(String type, String token) {
        this.type = type;
        this.token = token;
        this.task = TASK_GET_USER;
    }

    public SyncMaster(String type, String token, int task) {
        this.type = type;
        this.token = token;
        this.task = task;

    }

    public void setProfileSyncedCallback(IOperationFinishWithDataCallback profileSyncedCallback) {
        this.profileSyncedCallback = profileSyncedCallback;
    }

    @Override
    protected Void doInBackground(Void... params) {
        StringBuilder link = new StringBuilder();
        link.append(ServerApi.USER_SYNC);
        link.append("?token=");
        link.append(token);
        link.append("&type=");
        link.append(type);

        try {
            Request request = null;
            if (task == TASK_SYNC) {
                RequestBody formBody = new FormEncodingBuilder().
                        add("settings", SettingsManager.getInstace().getAsJson().toString()).
                        add("profile", ProfileManager.getInstance().getAsJson().toString()).build();
                request = new Request.Builder().url(link.toString()).post(formBody).build();
            } else {
                request = new Request.Builder().url(link.toString()).build();
            }
            result = BackendManager.getInstance().sendSynchronicRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (task == TASK_SYNC) {
            Logger.printInfo("SyncMaster", "Saved to cloud");
        }
        if (profileSyncedCallback != null) {
            profileSyncedCallback.operationFinished(result);
        }
    }

    public static void saveToCloud() {
        SyncMaster profileSyncMaster = new SyncMaster(LoginMaster.getInstance().getLoginType(),
                LoginMaster.getInstance().getToken(), SyncMaster.TASK_SYNC);
        profileSyncMaster.execute();
    }
}
