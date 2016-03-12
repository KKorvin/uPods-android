package com.chickenkiller.upods2;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.chickenkiller.upods2.controllers.internet.SyncMaster;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;

import org.junit.runner.RunWith;

/**
 * Created by alonzilberman on 3/9/16.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CloudSyncTest {

    public void testGlobalTokenScenario(){
        SyncMaster profileSyncMaster = new SyncMaster("global", String.valueOf(System.currentTimeMillis()), "", SyncMaster.TASK_SYNC);
        profileSyncMaster.setProfileSyncedCallback(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {

            }
        });
        profileSyncMaster.execute();
    }

}
