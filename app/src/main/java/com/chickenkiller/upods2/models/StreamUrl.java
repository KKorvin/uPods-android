package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
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

    StreamUrl(String url) {
        this.url = url;
        this.isAlive = true;
    }

    StreamUrl(JSONObject jsonItem) {
        try {
            this.url = jsonItem.has("url") ? jsonItem.getString("url") : "";
            this.bitrate = jsonItem.has("bitrate") ? jsonItem.getString("bitrate") : "";
            this.isAlive = true;

            if (bitrate.equals("null")) {
                this.bitrate = "";
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

    public static StreamUrl getBestStreamUrl(Set<StreamUrl> allUrls) {
        StreamUrl emptyStreamUrl = null;
        for (String pattern : bestStreamPatterns) {
            for (StreamUrl streamUrl : allUrls) {
                if (streamUrl.isAlive && streamUrl.getUrl().matches(pattern)) {
                    return streamUrl;
                } else if (streamUrl.isAlive && emptyStreamUrl == null) {
                    emptyStreamUrl = streamUrl;
                }
            }
        }
        if (emptyStreamUrl == null) {
            emptyStreamUrl = new StreamUrl("");
        }
        return emptyStreamUrl;
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

