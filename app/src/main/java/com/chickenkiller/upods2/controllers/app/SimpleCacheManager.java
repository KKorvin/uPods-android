package com.chickenkiller.upods2.controllers.app;

import android.util.Log;

import java.math.BigInteger;

/**
 * Created by alonzilberman on 10/28/15.
 */
public class SimpleCacheManager {

    private static final String LOG_TAG = "SimpleCacheManager";
    private static final String FILE_NAME_SEPARATOR = "_";
    private static SimpleCacheManager cacheManager;

    private SimpleCacheManager() {

    }

    public static SimpleCacheManager getInstance() {
        if (cacheManager == null) {
            cacheManager = new SimpleCacheManager();
        }
        return cacheManager;
    }

    private String encodeFileName(String fileName) {
        BigInteger fileNameEncoded = new BigInteger(fileName.getBytes());
        return fileNameEncoded.toString();
    }

    private String decodeFileName(String fileName) {
        BigInteger fileNameEncoded = new BigInteger(fileName.getBytes());
        String fileNameDecoded = new String(fileNameEncoded.toString());
        return fileNameDecoded;
    }


    public void cacheUrlOutput(String url, String output) {
        String fileName = encodeFileName(url);
        fileName+= FILE_NAME_SEPARATOR + System.currentTimeMillis();
        Log.i("")
    }
}
