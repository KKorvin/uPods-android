package com.chickenkiller.upods2;

import android.os.AsyncTask;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.internet.SyncMaster;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.StreamUrl;
import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Created by alonzilberman on 3/9/16.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CloudSyncTest {

    @Test
    public void testGlobalTokenScenario() {
        //Add subscribed -> Sync with server - > remove subscribed -> get profile from server -> subscribed should be there

        String radioName = String.valueOf(System.currentTimeMillis()) + "_radio";
        final RadioItem radioItem = new RadioItem(radioName, new StreamUrl(""), "");

        ProfileManager.getInstance().addSubscribedMediaItem(radioItem);
        boolean isRadiotInFavorites = ProfileManager.getInstance().isSubscribedToMediaItem(radioItem);
        Logger.printInfo("testGlobalTokenScenario", "Adding radio to subscribed");
        assertTrue(isRadiotInFavorites);

        final String globalToken = String.valueOf(System.currentTimeMillis());

        final SyncMaster profileSyncMasterGET = new SyncMaster("global", globalToken, "", SyncMaster.TASK_GET_USER);

        final SyncMaster profileSyncMasterSYNC = new SyncMaster("global", globalToken, "", SyncMaster.TASK_SYNC);
        profileSyncMasterSYNC.setProfileSyncedCallback(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                Logger.printInfo("testGlobalTokenScenario", "Synced with cloud SYNC-> removing subscribed radioItem");
                ProfileManager.getInstance().removeSubscribedMediaItem(radioItem);
                boolean isRadiotInFavorites = ProfileManager.getInstance().isSubscribedToMediaItem(radioItem);
                assertTrue(!isRadiotInFavorites);

                profileSyncMasterGET.setProfileSyncedCallback(new IOperationFinishWithDataCallback() {
                    @Override
                    public void operationFinished(Object data) {
                        boolean isRadiotInFavorites = false;
                        try {
                            Logger.printInfo("testGlobalTokenScenario", "Synced with cloud GET -> checking subscribed radioItem");
                            if (((JSONObject) data).getJSONObject("result").has("profile")) {
                                JSONObject profile = new JSONObject(((JSONObject) data).getJSONObject("result").getString("profile"));
                                ProfileManager.getInstance().readFromJson(profile);
                                isRadiotInFavorites = ProfileManager.getInstance().isSubscribedToMediaItem(radioItem);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        assertTrue(isRadiotInFavorites);
                    }
                });
                profileSyncMasterGET.execute();
            }
        });
        profileSyncMasterSYNC.execute();
        while (profileSyncMasterGET.getStatus() != AsyncTask.Status.FINISHED) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
