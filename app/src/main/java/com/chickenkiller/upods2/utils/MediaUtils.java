package com.chickenkiller.upods2.utils;

import android.util.Log;

import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.ISimpleRequestCallback;

import java.util.List;

/**
 * Created by alonzilberman on 10/30/15.
 */
public class MediaUtils {

    private static final String LOG_TAG = "MediaUtils";

    public static String extractMp3FromM3u(final String m3uUrl, final IOperationFinishWithDataCallback mp3Extracted) {
        BackendManager.getInstance().sendRequest(m3uUrl, new ISimpleRequestCallback() {
            @Override
            public void onRequestSuccessed(String response) {
                List<String> allURls = GlobalUtils.extractUrls(response);
                String mp3Url = allURls.size() > 0 ? allURls.get(0) : "";
                Log.i(LOG_TAG, "Extracted from m3u urls: " + allURls.toString());
                mp3Extracted.operationFinished(mp3Url);
            }

            @Override
            public void onRequestFailed() {

            }
        });
        return "";
    }
}
