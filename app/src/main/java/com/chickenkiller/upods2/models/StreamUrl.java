package com.chickenkiller.upods2.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Alon Zilberman on 12/11/15.
 */
public class StreamUrl extends SQLModel {

    public static final String TABLE = "stream_link";
    private static final String STREAM_URL_LOG = "StreamUrl";
    private static final String[] bestStreamPatterns = {".+\\.mp3", ".+[^.]{4}$"};

    private String url;
    private String bitrate;
    public boolean isAlive;

    public StreamUrl() {
        super();
        this.isAlive = true;
        this.bitrate = "";
        this.url = "";
    }

    public StreamUrl(String url) {
        this();
        this.url = url;
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

    public static StreamUrl getBestStreamUrl(Set<StreamUrl> allUrls) {
        final List<StreamUrl> list = new ArrayList<StreamUrl>();
        SettingsManager settingsManager = SettingsManager.getInstace();

        String alreadySetQuality = settingsManager.getStreamQuality(UniversalPlayer.getInstance().getPlayingMediaItem());
        for (StreamUrl streamUrl : allUrls) {
            if (streamUrl.isAlive && streamUrl.hasBitrate()) {
                list.add(streamUrl);
                if (alreadySetQuality != null && streamUrl.bitrate.equals(alreadySetQuality)) {
                    return streamUrl; //If user already selected quality for this stream -> take it
                }
            }
        }
        if (list.size() > 0) { //It contains streams with stream quality -> take them
            String tempQuality = settingsManager.getStringSettingValue(SettingsManager.JS_STREAM_QUALITY);
            final String neededQuality = tempQuality.isEmpty() ? SettingsManager.DEFAULT_STREAM_QUALITY : tempQuality;

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

    public static void saveSet(Set<StreamUrl> streamUrlSet, long radioStationId) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        database.beginTransaction();
        for (StreamUrl streamUrl : streamUrlSet) {
            ContentValues values = new ContentValues();
            values.put("radio_station_id", radioStationId);
            values.put("url", streamUrl.getUrl());
            values.put("bitrate", streamUrl.getBitrate());
            streamUrl.id = database.insert(TABLE, null, values);
            streamUrl.isExistsInDb = true;
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public static Set<StreamUrl> withRaioItemId(long radioId) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        Set<StreamUrl> streamUrls = new HashSet<>();

        String args[] = {String.valueOf(radioId)};
        Cursor cursor = database.rawQuery("SELECT * FROM stream_link WHERE radio_station_id = ?", args);
        while (cursor.moveToNext()) {
            StreamUrl streamUrl = new StreamUrl();
            streamUrl.isExistsInDb = true;
            streamUrl.id = cursor.getLong(cursor.getColumnIndex("id"));
            streamUrl.url = cursor.getString(cursor.getColumnIndex("url"));
            streamUrl.bitrate = cursor.getString(cursor.getColumnIndex("bitrate"));
            streamUrls.add(streamUrl);
        }
        return streamUrls;
    }
}

