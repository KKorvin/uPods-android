package com.chickenkiller.upods2.utils;

import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.ISimpleRequestCallback;

import java.sql.Time;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by alonzilberman on 10/30/15.
 */
public class MediaUtils {

    private static final String LOG_TAG = "MediaUtils";

    public static String extractMp3FromFile(final String m3uUrl, final IOperationFinishWithDataCallback mp3Extracted) {
        BackendManager.getInstance().sendRequest(m3uUrl, new ISimpleRequestCallback() {
            @Override
            public void onRequestSuccessed(String response) {
                List<String> allURls = GlobalUtils.extractUrls(response);
                String mp3Url = allURls.size() > 0 ? allURls.get(0) : "";
                Logger.printInfo(LOG_TAG, "Extracted from file urls: " + allURls.toString());
                mp3Extracted.operationFinished(mp3Url);
            }

            @Override
            public void onRequestFailed() {

            }
        });
        return "";
    }

    public static String formatMsToTimeString(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public static long timeStringToLong(String timeString) {
        if(timeString.matches("[0-9]{2}:[0-9]{2}")){
            timeString = "00:" + timeString;
        }
        Time t = Time.valueOf(timeString);
        long l = t.getTime();
        return l;
    }

}
