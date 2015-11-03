package com.chickenkiller.upods2.controllers.app;

import com.chickenkiller.upods2.utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by alonzilberman on 10/28/15.
 */
public class SimpleCacheManager {

    private static final String LOG_TAG = "SimpleCacheManager";
    private static final String CACHE_FOLDER = "/simple_cache/";
    private static final String FILE_NAME_SEPARATOR = "_";
    private static final String FILE_NAME_SHORTIFY_REGEX = "([^a-z,^A-Z,0-9])|(https)|(http)";
    private static final long DEFAULT_CACHE_PERIOD = 900000; //in ms

    private static SimpleCacheManager cacheManager;

    private SimpleCacheManager() {
        File cacheFolder = new File(UpodsApplication.getContext().getFilesDir() + CACHE_FOLDER);
        try {
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs();
                Logger.printInfo(LOG_TAG, "Cache folder created: " + cacheFolder.getAbsolutePath());
            }
        } catch (Exception e) {
            Logger.printInfo(LOG_TAG, "Can't create cache folder");
            e.printStackTrace();
        }
    }

    public static SimpleCacheManager getInstance() {
        if (cacheManager == null) {
            cacheManager = new SimpleCacheManager();
        }
        return cacheManager;
    }

    private String shortifyFileName(String fileName) {
        return fileName.replaceAll(FILE_NAME_SHORTIFY_REGEX, "");
    }

    public void cacheUrlOutput(String url, String output) {
        cacheUrlOutput(url, output, DEFAULT_CACHE_PERIOD);
    }

    public void cacheUrlOutput(String url, String output, long cachePeriod) {
        //if (existsInCache(output)) {
        //    return;
       // }
        String fileName = shortifyFileName(url);
        fileName += FILE_NAME_SEPARATOR + String.valueOf(cachePeriod) + FILE_NAME_SEPARATOR + System.currentTimeMillis();
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(UpodsApplication.getContext().getFilesDir() + CACHE_FOLDER + fileName);
            outputStream.write(output.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.printInfo(LOG_TAG, "Url: " + url + " cached to file: " + fileName);
    }

    public String readFromCache(String url) {
        Logger.printInfo(LOG_TAG, "Looking for url: " + url + " in cache...");
        url = url.replaceAll(FILE_NAME_SHORTIFY_REGEX, "");
        File cacheStorage = new File(UpodsApplication.getContext().getFilesDir() + CACHE_FOLDER);
        if (cacheStorage != null && cacheStorage.list() != null) {
            for (String fileName : cacheStorage.list()) {
                if (fileName.contains(url) && !isExpired(fileName)) {
                    File cacheFile = new File(UpodsApplication.getContext().getFilesDir() + CACHE_FOLDER + fileName);
                    StringBuilder text = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(cacheFile));
                        String line;

                        while ((line = br.readLine()) != null) {
                            if (line.length() > 0) {
                                text.append(line);
                            }
                        }
                        br.close();
                    } catch (IOException e) {
                        //You'll need to add proper error handling here
                    }
                    Logger.printInfo(LOG_TAG, "Found cache!");
                    return text.toString();
                }
            }
        }
        return null;
    }

    public boolean existsInCache(String url) {
        url = url.replaceAll(FILE_NAME_SHORTIFY_REGEX, "");
        File cacheStorage = new File(UpodsApplication.getContext().getFilesDir() + CACHE_FOLDER);
        if (cacheStorage != null && cacheStorage.list() != null) {
            for (String fileName : cacheStorage.list()) {
                if (fileName.contains(url) && !isExpired(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExpired(String fileName) {
        String[] dates = fileName.split("_");
        if (dates != null && dates.length > 2) {
            long period = Long.valueOf(dates[1]);
            long createdAt = Long.valueOf(dates[2]);
            return System.currentTimeMillis() > createdAt + period;
        }
        return true;
    }

    public void removeExpiredCache() {
        try {
            int count = 0;
            File cacheStorage = new File(UpodsApplication.getContext().getFilesDir() + CACHE_FOLDER);
            if (cacheStorage != null && cacheStorage.list() != null) {
                for (String fileName : cacheStorage.list()) {
                    if (isExpired(fileName)) {
                        File cacheFile = new File(UpodsApplication.getContext().getFilesDir() + CACHE_FOLDER + fileName);
                        cacheFile.delete();
                        count++;
                    }
                }
            }
            Logger.printInfo(LOG_TAG, "Removed " + count + " expired cache files ");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
