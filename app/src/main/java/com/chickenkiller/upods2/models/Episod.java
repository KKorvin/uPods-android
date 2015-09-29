package com.chickenkiller.upods2.models;

import android.text.format.Formatter;
import android.util.Log;

import com.chickenkiller.upods2.controllers.UpodsApplication;
import com.chickenkiller.upods2.interfaces.IPlayableTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 8/31/15.
 */
public class Episod extends Track implements IPlayableTrack {
    private static String EPISOD_LOG = "EPISOD";
    private String summary;
    private String length;
    private String duration;
    private String btnDownloadText;
    private String date;
    private boolean isDownloaded;

    public Episod() {
        super();
        this.summary = "";
        this.length = "";
        this.duration = "";
        this.btnDownloadText = "";
        this.date = "";
        this.isDownloaded = false;
    }

    public Episod(JSONObject jsonItem) {
        this();
        try {
            this.summary = jsonItem.has("summary") ? jsonItem.getString("summary") : "";
            this.length = jsonItem.has("length") ? jsonItem.getString("length") : "";
            this.duration = jsonItem.has("duration") ? jsonItem.getString("duration") : "";
            this.date = jsonItem.has("date") ? jsonItem.getString("date") : "";
            this.isDownloaded = jsonItem.has("isDownloaded") ? jsonItem.getBoolean("isDownloaded") : false;
        } catch (JSONException e) {
            Log.e(EPISOD_LOG, "Can't parse episod from json");
            e.printStackTrace();
        }

    }

    public JSONObject toJson() {
        JSONObject jsonEpisod = new JSONObject();
        try {
            jsonEpisod.put("summary", this.summary);
            jsonEpisod.put("length", this.length);
            jsonEpisod.put("duration", this.duration);
            jsonEpisod.put("date", this.date);
            jsonEpisod.put("isDownloaded", isDownloaded);
        } catch (JSONException e) {
            Log.e(EPISOD_LOG, "Can't save episod to json");
            e.printStackTrace();
        }
        return jsonEpisod;
    }

    public static JSONArray toJSONArray(ArrayList<Episod> episods) {
        JSONArray episodsJson = new JSONArray();
        for (Episod episod : episods) {
            episodsJson.put(episod.toJson());
        }
        return episodsJson;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubTitle() {
        String size = Formatter.formatShortFileSize(UpodsApplication.getContext(), Long.valueOf(length));
        return duration + " / " + size;
    }

    @Override
    public String getAudeoUrl() {
        return mp3Url;
    }

    public String getBtnDownloadText() {
        return btnDownloadText;
    }

    public void setBtnDownloadText(String btnDownloadText) {
        this.btnDownloadText = btnDownloadText;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String getDate() {
        return this.date;
    }

}
