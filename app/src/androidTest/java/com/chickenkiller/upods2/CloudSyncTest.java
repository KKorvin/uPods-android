package com.chickenkiller.upods2;

import android.os.AsyncTask;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.controllers.internet.SyncMaster;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.StreamUrl;
import com.chickenkiller.upods2.utils.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

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

        UpodsApplication.initAllResources();

        String radioName = String.valueOf(System.currentTimeMillis()) + "_radio";
        final RadioItem radioItem = new RadioItem(radioName, new StreamUrl(""), "");

        ProfileManager.getInstance().addSubscribedMediaItem(radioItem);
        Logger.printInfo("testGlobalTokenScenario", "Adding radio to subscribed");
        assertTrue(MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getSubscribedRadioItems(), radioItem));

        final String globalToken = String.valueOf(System.currentTimeMillis());

        final SyncMaster profileMasterGET = new SyncMaster("global", globalToken, "", SyncMaster.Task.PULL);
        final SyncMaster profileSyncMasterPUSH = new SyncMaster("global", globalToken, "", SyncMaster.Task.PUSH);
        profileSyncMasterPUSH.setProfileSyncedCallback(new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                Logger.printInfo("testGlobalTokenScenario", "Synced with cloud SYNC-> removing subscribed radioItem");
                ProfileManager.getInstance().removeSubscribedMediaItem(radioItem);
                assertTrue(!MediaItem.hasMediaItemWithName(ProfileManager.getInstance().getSubscribedRadioItems(), radioItem));

                profileMasterGET.setProfileSyncedCallback(new IOperationFinishCallback() {
                    @Override
                    public void operationFinished() {
                        ArrayList<RadioItem> getSubscribedRatioItems = ProfileManager.getInstance().getSubscribedRadioItems();
                        assertTrue(MediaItem.hasMediaItemWithName(getSubscribedRatioItems, radioItem));
                    }
                });

                profileMasterGET.execute();
            }
        });

        profileSyncMasterPUSH.execute();
        while (profileMasterGET.getStatus() != AsyncTask.Status.FINISHED) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
