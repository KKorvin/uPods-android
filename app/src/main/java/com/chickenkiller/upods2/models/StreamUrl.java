package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by alonzilberman on 12/11/15.
 */
public class StreamUrl implements Serializable {

    private static final String STREAM_URL_LOG = "StreamUrl";
    private static final String[] bestStreamPatterns = {".+\\.mp3", ".+[^.]{4}$"};

    private String url;
    private String bitrate;
    public boolean isAlive;

    public StreamUrl(String url) {
        this.url = url;
        this.isAlive = true;
        this.bitrate = "";
    }

    public StreamUrl(JSONObject jsonItem) {
        try {
            this.url = jsonItem.has("url") ? jsonItem.getString("url") : "";
            this.bitrate = jsonItem.has("bitrate") ? jsonItem.getString("bitrate") : "";
            this.isAlive = true;

            if (bitrate.contains("000")) {
                bitrate = bitrate.replace("000", "");
            }

            if (bitrate.equals("null") || bitrate.equals("0")) {
                bitrate = "";
            }
            if (!bitrate.matches("[0-9]+")) {
                bitrate = "";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() {
        JSONObject jsonItem = new JSONObject();
        try {
            jsonItem.put("url", this.url);
            jsonItem.put("bitrate", this.bitrate);
        } catch (JSONException e) {
            Logger.printError(STREAM_URL_LOG, "Can't convert StreamUrl to json");
            e.printStackTrace();
        }
        return jsonItem;
    }

    public String getUrl() {
        return url;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean hasBitrate() {
        return bitrate != null && !bitrate.isEmpty();
    }


    public static StreamUrl getBestStreamUrl(Set<StreamUrl> allUrls) {
        final List<StreamUrl> list = new ArrayList<StreamUrl>();
        for (StreamUrl streamUrl : allUrls) {
            if (streamUrl.isAlive && streamUrl.hasBitrate()) {
                list.add(streamUrl);
            }
        }
        if (list.size() > 0) { //I contains streams with stream quality -> take them
            final String neededQuality = SettingsManager.getInstace().getStringSettingValue(SettingsManager.JS_STREAM_QUALITY);
            Collections.sort(list, new Comparator<StreamUrl>() {
                public int compare(StreamUrl a, StreamUrl b) {
                    if (neededQuality.equals(SettingsManager.DEFAULT_STREAM_QUALITY)) {
                        return Integer.parseInt(b.bitrate) - Integer.parseInt(a.bitrate);
                    } else {
                        return Integer.parseInt(a.bitrate) - Integer.parseInt(b.bitrate);
                    }
                }
            });
            return list.get(0);
        } else { //Take any available stream
            for (StreamUrl streamUrl : allUrls) {
                if (streamUrl.isAlive && !streamUrl.hasBitrate()) {
                    list.add(streamUrl);
                }
            }
            if (list.size() > 0) {
                return list.get(0);
            }
        }
        return new StreamUrl("");
    }

    public static Set<String> getAllAvailableStreams(Set<StreamUrl> allUrls) {
        Set<String> allStreams = new HashSet<>();
        for (StreamUrl streamUrl : allUrls) {
            if (streamUrl.isAlive && streamUrl.hasBitrate()) {
                allStreams.add(streamUrl.bitrate);

            }
        }
        return allStreams;
    }

    public static void replaceUrl(Set<StreamUrl> streamUrls, String newUrl, String oldUrl) {
        for (StreamUrl streamUrl : streamUrls) {
            if (streamUrl.getUrl().equals(oldUrl)) {
                streamUrl.setUrl(newUrl);
                return;
            }
        }
    }
}

