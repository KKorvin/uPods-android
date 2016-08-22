package com.chickenkiller.upods2.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Formatter;

import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Alon Zilberman on 8/31/15.
 */
public class Episode extends Track {
    private static final String TABLE = "episodes";
    private static String EPISODE_LOG = "EPISODE";
    private String summary;
    private String length;
    private String duration;
    private String date;
    private String pathOnDisk;

    //Not in DB
    public boolean isNew;
    public boolean isDownloaded;

    public Episode() {
        super();
        this.summary = "";
        this.length = "";
        this.duration = "";
        this.date = "";
        this.pathOnDisk = "";
        this.isNew = false;
        this.isDownloaded = false;
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

    public long save(long podcastId) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("podcast_id", podcastId);
        values.put("title", title);
        values.put("summary", summary);
        values.put("length", length);
        values.put("duration", duration);
        values.put("date", date);
        values.put("pathOnDisk", pathOnDisk);

        id = database.insert(TABLE, null, values);
        isExistsInDb = true;

        return id;
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

    public static JSONArray toJSONArray(ArrayList<Episode> episodes, boolean onlySavedInDb) {
        JSONArray episodesJson = new JSONArray();
        for (Episode episode : episodes) {
            if (onlySavedInDb && (episode.isDownloaded || episode.isNew)) {
                episodesJson.put(episode.toJson());
            } else if (!onlySavedInDb) {
                episodesJson.put(episode.toJson());
            }
        }
        return episodesJson;
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

    public void remove() {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        String args[] = {String.valueOf(id)};
        database.delete("episodes", "id = ?", args);
        isDownloaded = false;
        isNew = false;
        isExistsInDb = false;
        id = -1;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubTitle() {
        String size;
        try {
            size = Formatter.formatShortFileSize(UpodsApplication.getContext(), Long.valueOf(length));
        } catch (NumberFormatException e) {
            size = length;
        }
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

    public static void saveList(long podcastId, ArrayList<Episode> episodes) {
        if (episodes.size() > 0) {
            SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
            database.beginTransaction();
            for (Episode episode : episodes) {
                if (episode.isDownloaded || episode.isNew) {
                    episode.save(podcastId);
                }
            }
            database.endTransaction();
        }
    }

    public static ArrayList<Episode> withPodcastId(long podcastId) {
        ArrayList<Episode> allEpisodes = new ArrayList<>();
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
        String args[] = {String.valueOf(podcastId)};
        Cursor cursor = database.rawQuery("SELECT e.*, per.type FROM podcasts_episodes_rel as per\n" +
                "LEFT JOIN episodes  as e \n" +
                "ON e.id = per.episode_id WHERE per.podcast_id = ?", args);
        while (cursor.moveToNext()) {
            Episode episode = Episode.getEpisodByTitle(allEpisodes, cursor.getString(cursor.getColumnIndex("title")));
            if (episode == null) {
                episode = new Episode();
                episode.isExistsInDb = true;
                episode.id = cursor.getLong(cursor.getColumnIndex("id"));
                episode.title = cursor.getString(cursor.getColumnIndex("title"));
                episode.summary = cursor.getString(cursor.getColumnIndex("summary"));
                episode.length = cursor.getString(cursor.getColumnIndex("length"));
                episode.duration = cursor.getString(cursor.getColumnIndex("duration"));
                episode.date = cursor.getString(cursor.getColumnIndex("date"));
                episode.pathOnDisk = cursor.getString(cursor.getColumnIndex("pathOnDisk"));
            }

            if (cursor.getString(cursor.getColumnIndex("type")).equals(MediaListItem.DOWNLOADED)) {
                episode.isDownloaded = true;
            } else if (cursor.getString(cursor.getColumnIndex("type")).equals(MediaListItem.NEW)) {
                episode.isNew = true;
            }

            allEpisodes.add(episode);
        }
        cursor.close();
        return allEpisodes;
    }

    public static boolean hasEpisodWithTitle(ArrayList<Episode> episodes, Episode episodeToCheck) {
        for (Episode episode : episodes) {
            if (GlobalUtils.safeTitleEquals(episode.getTitle(), episodeToCheck.getTitle())) {
                return true;
            }
        }
        return false;
    }

    public static Episode getEpisodByTitle(ArrayList<Episode> episodes, String titleToCheck) {
        for (Episode episode : episodes) {
            if (GlobalUtils.safeTitleEquals(episode.getTitle(), (titleToCheck))) {
                return episode;
            }
        }
        return null;
    }

    public static void syncDbWithLatestEpisodes(Podcast podcast, ArrayList<Episode> currentEpisodes) {
        ArrayList<Episode> episodesInDb = withPodcastId(podcast.id);
        for (Episode episode : episodesInDb) {
            Episode episodeInCurrentFeed = Episode.getEpisodByTitle(currentEpisodes, episode.getTitle());
            if (episodeInCurrentFeed == null) {
                ProfileManager.getInstance().removeNewTrack(podcast, episode);
            }
        }
    }

}
