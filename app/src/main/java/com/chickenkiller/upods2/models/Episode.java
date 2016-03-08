package com.chickenkiller.upods2.models;

import android.text.format.Formatter;

import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 8/31/15.
 */
public class Episode extends Track {
    private static String EPISODE_LOG = "EPISODE";
    private String summary;
    private String length;
    private String duration;
    private String btnDownloadText;
    private String date;
    private String pathOnDisk;
    public boolean isNotNew; //Used to notify adaper about changes in new episodes

    public Episode() {
        super();
        this.summary = "";
        this.length = "";
        this.duration = "";
        this.btnDownloadText = "";
        this.date = "";
        this.pathOnDisk = "";
        this.isNotNew = false;
    }

    public Episode(JSONObject jsonItem) {
        this();
        try {
            this.title = jsonItem.has("title") ? jsonItem.getString("title") : "";
            this.title = this.title.replace("\n", "").trim();
            this.summary = jsonItem.has("summary") ? jsonItem.getString("summary") : "";
            this.length = jsonItem.has("length") ? jsonItem.getString("length") : "";
            this.duration = jsonItem.has("duration") ? jsonItem.getString("duration") : "";
            this.date = jsonItem.has("date") ? jsonItem.getString("date") : "";
            this.pathOnDisk = jsonItem.has("pathOnDisk") ? jsonItem.getString("pathOnDisk") : "";
            this.audeoUrl = jsonItem.has("audeoUrl") ? jsonItem.getString("audeoUrl") : "";
        } catch (JSONException e) {
            Logger.printError(EPISODE_LOG, "Can't parse episod from json");
            e.printStackTrace();
        }

    }

    public static boolean hasEpisodWithTitle(ArrayList<Episode> episodes, Episode episodeToCheck) {
        for (Episode episode : episodes) {
            if (episode.getTitle().replace("\n", "").equals(episodeToCheck.getTitle().replace("\n", ""))) {
                return true;
            }
        }
        return false;
    }

    public static Episode getEpisodByTitle(ArrayList<Episode> episodes, Episode episodeToCheck) {
        for (Episode episode : episodes) {
            if (episode.getTitle().equals(episodeToCheck.getTitle())) {
                return episode;
            }
        }
        return null;
    }

    public JSONObject toJson() {
        JSONObject jsonEpisod = new JSONObject();
        try {
            jsonEpisod.put("title", this.title);
            jsonEpisod.put("summary", this.summary);
            jsonEpisod.put("length", this.length);
            jsonEpisod.put("duration", this.duration);
            jsonEpisod.put("date", this.date);
            jsonEpisod.put("pathOnDisk", this.pathOnDisk);
            jsonEpisod.put("audeoUrl", this.audeoUrl);
        } catch (JSONException e) {
            Logger.printError(EPISODE_LOG, "Can't save episod to json");
            e.printStackTrace();
        }
        return jsonEpisod;
    }

    public static JSONArray toJSONArray(ArrayList<Episode> episodes) {
        JSONArray episodesJson = new JSONArray();
        for (Episode episode : episodes) {
            episodesJson.put(episode.toJson());
        }
        return episodesJson;
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
        if (duration.matches("^[0-9]{3,5}$")) {//Time was given in seconds
            duration = MediaUtils.formatMsToTimeString(Integer.valueOf(duration) * 1000);
        }
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

    public void setPathOnDisk(String pathOnDisk) {
        this.pathOnDisk = pathOnDisk;
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
    public String getInfo() {
        return summary;
    }

    @Override
    public String getAudeoUrl() {
        if (pathOnDisk != null && !pathOnDisk.isEmpty()) {
            return pathOnDisk;
        }
        return audeoUrl;
    }

    @Override
    public String getDate() {
        return this.date;
    }

}
