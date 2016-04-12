package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.SimpleCacheManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by alonzilberman on 3/7/16.
 */
public class Feed {

    private static final String FEEDS_FOLDER = "/feeds/";
    private static final String LOG_TAG = "Feed";
    private static boolean isUpdating = false;

    private String url;
    private ArrayList<Episode> episodes;


    private Feed() {
        File cacheFolder = new File(UpodsApplication.getContext().getFilesDir() + FEEDS_FOLDER);
        try {
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs();
                Logger.printInfo(LOG_TAG, "Feed folder created: " + cacheFolder.getAbsolutePath());
            }
        } catch (Exception e) {
            Logger.printInfo(LOG_TAG, "Feed create cache folder");
            e.printStackTrace();
        }
    }

    private Feed(JSONObject jsonObject) {
        this();
        try {
            this.episodes = new ArrayList<>();
            this.url = jsonObject.has("url") ? jsonObject.getString("url") : "";
            if (jsonObject.has("episodes")) {
                JSONArray jsonEpisodes = jsonObject.getJSONArray("episodes");
                for (int i = 0; i < jsonEpisodes.length(); i++) {
                    this.episodes.add(new Episode(jsonEpisodes.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Feed(String url, ArrayList<Episode> episodes) {
        this();
        this.url = url;
        this.episodes = new ArrayList<>();
        this.episodes.addAll(episodes);
    }

    public JSONObject toJson() {
        JSONObject feedJsonObject = new JSONObject();
        try {
            feedJsonObject.put("url", url);
            feedJsonObject.put("episodes", Episode.toJSONArray(episodes, false));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return feedJsonObject;
    }

    public static void saveAsFeed(final String url, final ArrayList<Episode> episodes, boolean isInBackground) {
        if (isInBackground) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Check if podcast with only downloaded episodes given -> fetch all feed
                    boolean fromDownloaded = true;
                    for (Episode episode : episodes) {
                        if (!episode.isDownloaded) {
                            fromDownloaded = false;
                            break;
                        }
                    }
                    if (fromDownloaded) {
                        ArrayList<Episode> parsedEpisodes = BackendManager.getInstance().fetchEpisodes(url);
                        saveAsFeed(url, parsedEpisodes);
                    } else {
                        saveAsFeed(url, episodes);
                    }
                }
            }).run();
        } else {
            saveAsFeed(url, episodes);
        }
    }

    public static void saveAsFeed(String url, ArrayList<Episode> episodes) {
        Feed feed = new Feed(url, episodes);
        JSONObject feedJsonObject = feed.toJson();
        String fileName = SimpleCacheManager.getInstance().shortifyFileName(url);
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(UpodsApplication.getContext().getFilesDir() + FEEDS_FOLDER + fileName);
            outputStream.write(feedJsonObject.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFeed(String url) {
        url = SimpleCacheManager.getInstance().shortifyFileName(url);
        File cacheStorage = new File(UpodsApplication.getContext().getFilesDir() + FEEDS_FOLDER);
        if (cacheStorage != null && cacheStorage.list() != null) {
            for (String fileName : cacheStorage.list()) {
                if (fileName.contains(url)) {
                    File cacheFile = new File(UpodsApplication.getContext().getFilesDir() + FEEDS_FOLDER + fileName);
                    cacheFile.delete();
                }
            }
        }
    }


    public static Feed getFeedIfExists(String url) {
        url = SimpleCacheManager.getInstance().shortifyFileName(url);
        File cacheStorage = new File(UpodsApplication.getContext().getFilesDir() + FEEDS_FOLDER);
        if (cacheStorage != null && cacheStorage.list() != null) {
            for (String fileName : cacheStorage.list()) {
                if (fileName.contains(url)) {
                    File cacheFile = new File(UpodsApplication.getContext().getFilesDir() + FEEDS_FOLDER + fileName);
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
                        return new Feed(new JSONObject(text.toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static synchronized boolean handleUpdates(ArrayList<Episode> latestEpisodes, Podcast podcast) {
        if (isUpdating) {
            return false;
        }
        isUpdating = true;
        //sync podcasts_episodes_rel and episodes with current episodes in feed to make sure we don't store old episodes
        Episode.syncDbWithLatestEpisodes(podcast, latestEpisodes);
        boolean hasUpdates = false;
        Feed feed = getFeedIfExists(podcast.getFeedUrl());
        if (feed != null && !feed.episodes.isEmpty()) {
            int i = 0;
            for (Episode latestEpisode : latestEpisodes) {
                if (!Episode.hasEpisodWithTitle(feed.episodes, latestEpisode)) {//Check if new eipsode is not saved in local feed
                    ProfileManager.getInstance().addNewTrack(podcast, latestEpisode);
                    hasUpdates = true;
                }
                if (i >= 5) {
                    break;
                }
                i++;
            }
            SimpleCacheManager.getInstance().removeFromCache(podcast.getFeedUrl());
        }
        saveAsFeed(podcast.getFeedUrl(), latestEpisodes);
        isUpdating = false;
        return hasUpdates;
    }

    public static void handleUpdates(Podcast podcast) {
        handleUpdates(podcast.getEpisodes(), podcast);
    }
}
