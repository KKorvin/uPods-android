package com.chickenkiller.upods2.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by alonzilberman on 8/24/15.
 */
public class Podcast extends MediaItem {
    public static final String TABLE = "podcasts";
    private static String PODCAST_LOG = "PODCAST";
    protected String name;
    protected String censoredName;
    protected String artistName;
    protected String feedUrl;
    protected String releaseDate;
    protected String explicitness;
    protected String trackCount;
    protected String country;
    protected String genre;
    protected String description;

    protected ArrayList<Episode> episodes;

    public boolean isDownloaded;
    public boolean hasNewEpisodes;

    public Podcast() {
        super();
        this.episodes = new ArrayList<>();
        this.censoredName = "";
        this.artistName = "";
        this.feedUrl = "";
        this.releaseDate = "";
        this.explicitness = "";
        this.trackCount = "0";
        this.genre = "";
        this.description = "";
    }

    public Podcast(String name, String feedUrl) {
        this();
        this.name = name.replace("\n", "").trim();
        this.feedUrl = feedUrl;
    }


    public Podcast(JSONObject jsonItem) {
        this();
        try {
            this.description = jsonItem.has("description") ? jsonItem.getString("description") : "";

            if (jsonItem.has("episodes")) {
                JSONArray jepisodes = jsonItem.getJSONArray("episodes");
                for (int i = 0; i < jepisodes.length(); i++) {
                    episodes.add(new Episode(jepisodes.getJSONObject(i)));
                }
            }

            if (jsonItem.has("kind")) { //Itunes
                this.id = jsonItem.has("trackId") ? jsonItem.getInt("trackId") : 0;
                this.name = jsonItem.has("collectionName") ? jsonItem.getString("collectionName") : "";
                this.name = name.replace("\n", "").trim();
                this.censoredName = jsonItem.has("collectionCensoredName") ? jsonItem.getString("collectionCensoredName") : "";
                this.artistName = jsonItem.has("artistName") ? jsonItem.getString("artistName") : "";
                this.feedUrl = jsonItem.has("feedUrl") ? jsonItem.getString("feedUrl") : "";
                this.coverImageUrl = jsonItem.has("artworkUrl600") ? jsonItem.getString("artworkUrl600") : "";
                if (this.coverImageUrl.isEmpty()) {
                    this.coverImageUrl = jsonItem.has("artworkUrl100") ? jsonItem.getString("artworkUrl100") : "";
                }
                if (this.coverImageUrl.isEmpty()) {
                    this.coverImageUrl = jsonItem.has("artworkUrl60") ? jsonItem.getString("artworkUrl60") : "";
                }
                this.country = jsonItem.has("country") ? jsonItem.getString("country") : "";
                this.releaseDate = jsonItem.has("releaseDate") ? jsonItem.getString("releaseDate") : "";
                this.explicitness = jsonItem.has("collectionExplicitness") ? jsonItem.getString("collectionExplicitness") : "";
                this.trackCount = jsonItem.has("trackCount") ? jsonItem.getString("trackCount") : "";
                this.genre = jsonItem.has("primaryGenreName") ? jsonItem.getString("primaryGenreName") : "";
            } else {//Our backend
                this.id = jsonItem.has("id") ? jsonItem.getInt("id") : 0;
                this.name = jsonItem.has("name") ? jsonItem.getString("name") : "";
                this.name = name.replace("\n", "").trim();
                this.censoredName = jsonItem.has("censored_name") ? jsonItem.getString("censored_name") : "";
                this.artistName = jsonItem.has("artist_name") ? jsonItem.getString("artist_name") : "";
                this.feedUrl = jsonItem.has("feed_url") ? jsonItem.getString("feed_url") : "";
                this.coverImageUrl = jsonItem.has("image_url") ? jsonItem.getString("image_url") : "";
                this.country = jsonItem.has("country") ? jsonItem.getString("country") : "";
                this.releaseDate = jsonItem.has("release_date") ? jsonItem.getString("release_date") : "";
                this.explicitness = jsonItem.has("explicitness") ? jsonItem.getString("explicitness") : "";
                this.trackCount = jsonItem.has("track_count") ? jsonItem.getString("track_count") : "";
                if (jsonItem.has("genres") && jsonItem.getJSONArray("genres").length() > 0) {
                    this.genre = jsonItem.getJSONArray("genres").getString(0);
                }
                if (jsonItem.has("episodes")) {
                    JSONArray jsonEpisodes = jsonItem.getJSONArray("episodes");
                    for (int i = 0; i < jsonEpisodes.length(); i++) {
                        this.episodes.add(new Episode(jsonEpisodes.getJSONObject(i)));
                    }
                }
            }
            this.name = this.name.replace("\n", "").trim();
        } catch (Exception e) {
            Logger.printError(PODCAST_LOG, "Can't parse podcast from json");
            e.printStackTrace();
        }
    }

    public Podcast(Podcast podcast) {
        this.name = podcast.getName().replace("\n", "").trim();
        this.censoredName = podcast.getCensoredName();
        this.artistName = podcast.getArtistName();
        this.feedUrl = podcast.getFeedUrl();
        this.coverImageUrl = podcast.getCoverImageUrl();
        this.releaseDate = podcast.getReleaseDate();
        this.explicitness = podcast.getExplicitness();
        this.trackCount = podcast.getTrackCount();
        this.country = podcast.getCountry();
        this.genre = podcast.getGenre();
        this.id = podcast.id;
        this.isExistsInDb = podcast.isExistsInDb;
        this.isSubscribed = podcast.isSubscribed;
        this.isDownloaded = podcast.isDownloaded;
        this.hasNewEpisodes = podcast.hasNewEpisodes;
        this.episodes = new ArrayList<Episode>(podcast.episodes);
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getCensoredName() {
        return censoredName;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(String trackCount) {
        this.trackCount = trackCount;
    }

    public String getExplicitness() {
        return explicitness;
    }

    public String getCountry() {
        return country;
    }

    public String getGenre() {
        return genre;
    }

    public ArrayList<Episode> getEpisodes() {
        return episodes;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTracks(ArrayList<? extends Track> tracks) {
        if (this.episodes != null) {
            this.episodes.clear();
        }
        this.episodes = (ArrayList<Episode>) tracks;
    }

    public ArrayList<? extends Track> getTracks() {
        return this.episodes;
    }

    public String getTracksFeed() {
        return this.feedUrl;
    }

    public Track getSelectedTrack() {
        for (Episode episode : episodes) {
            if (episode.isSelected) {
                return episode;
            }
        }
        return null;
    }

    public int getNewEpisodsCount() {
        int count = 0;
        for (Episode episode : episodes) {
            if (episode.isNew) {
                count++;
            }
        }
        return count;
    }

    public int getDownloadedEpisodsCount() {
        int count = 0;
        for (Episode episode : episodes) {
            if (episode.isDownloaded) {
                count++;
            }
        }
        return count;
    }

    public String getDownloadedDirectory() {
        for (Episode episode : episodes) {
            if (episode.isDownloaded) {
                File podcastFolder = new File(episode.getAudeoUrl());
                return podcastFolder.getParentFile().getPath();
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCoverImageUrl() {
        return coverImageUrl != null && coverImageUrl.isEmpty() ? null : coverImageUrl;
    }

    @Override
    public String getSubHeader() {
        return this.artistName;
    }

    @Override
    public String getBottomHeader() {
        return this.country;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAudeoLink() {
        for (Episode episode : episodes) {
            if (episode.isSelected) {
                return episode.getAudeoUrl();
            }
        }
        return episodes.get(0).getAudeoUrl();
    }

    @Override
    public String getBitrate() {
        return "";
    }

    @Override
    public boolean hasTracks() {
        return true;
    }

    public long save() {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("censored_name", censoredName);
        values.put("artist_name", artistName);
        values.put("description", description);
        values.put("feed_url", feedUrl);
        values.put("cover_image_url", coverImageUrl);
        values.put("release_date", releaseDate);
        values.put("explicitness", explicitness);
        values.put("country", country);
        values.put("genre", genre);
        values.put("track_count", trackCount);

        id = database.insert(TABLE, null, values);
        isExistsInDb = true;

        return id;
    }

    public JSONObject toJSON() {
        JSONObject podcast = new JSONObject();
        try {
            podcast.put("id", this.id);
            podcast.put("name", this.name);
            podcast.put("censored_name", this.censoredName);
            podcast.put("artist_name", this.artistName);
            podcast.put("feed_url", this.feedUrl);
            podcast.put("image_url", this.coverImageUrl);
            podcast.put("country", this.country);
            podcast.put("release_date", this.releaseDate);
            podcast.put("explicitness", this.explicitness);
            podcast.put("track_count", this.trackCount);
            podcast.put("genre", this.genre);
            podcast.put("description", this.description);
            //podcast.put("episodes", Episode.toJSONArray(this.episodes, true));
        } catch (JSONException e) {
            Logger.printError(PODCAST_LOG, "Can't convert podcast to json");
            e.printStackTrace();
        }
        return podcast;
    }

    public void selectTrack(Track track) {
        for (Episode episode : episodes) {
            episode.isSelected = episode.equals(track) ? true : false;
        }
    }

    public static boolean isExistsWithName(String name) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
        String[] args = {name};
        Cursor cursor = database.rawQuery("SELECT * from podcasts where name = ? LIMIT 1", args);
        cursor.moveToFirst();
        return cursor.getCount() > 0;
    }

    public static JSONArray toJsonArray(ArrayList<Podcast> podcasts) {
        JSONArray jsonPodcasts = new JSONArray();
        for (Podcast podcast : podcasts) {
            jsonPodcasts.put(podcast.toJSON());
        }
        return jsonPodcasts;
    }

    public static Podcast withCursor(Cursor cursor) {
        Podcast podcast = new Podcast();
        podcast.isExistsInDb = true;
        podcast.id = cursor.getLong(cursor.getColumnIndex("id"));
        podcast.name = cursor.getString(cursor.getColumnIndex("name"));
        podcast.censoredName = cursor.getString(cursor.getColumnIndex("censored_name"));
        podcast.artistName = cursor.getString(cursor.getColumnIndex("artist_name"));
        podcast.description = cursor.getString(cursor.getColumnIndex("description"));
        podcast.feedUrl = cursor.getString(cursor.getColumnIndex("feed_url"));
        podcast.coverImageUrl = cursor.getString(cursor.getColumnIndex("cover_image_url"));
        podcast.releaseDate = cursor.getString(cursor.getColumnIndex("release_date"));
        podcast.explicitness = cursor.getString(cursor.getColumnIndex("explicitness"));
        podcast.country = cursor.getString(cursor.getColumnIndex("country"));
        podcast.genre = cursor.getString(cursor.getColumnIndex("genre"));
        podcast.trackCount = cursor.getString(cursor.getColumnIndex("track_count"));

        podcast.episodes.addAll(Episode.withPodcastId(podcast.id));

        return podcast;
    }

    public static ArrayList<Podcast> withJsonArray(JSONArray jsonPodcastsItems) {
        ArrayList<Podcast> items = new ArrayList<Podcast>();
        try {
            for (int i = 0; i < jsonPodcastsItems.length(); i++) {
                JSONObject podcastItem = (JSONObject) jsonPodcastsItems.get(i);
                items.add(new Podcast(podcastItem));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public void syncWithMediaItem(MediaItem updatedMediaItem) {
        super.syncWithMediaItem(updatedMediaItem);
        Podcast updatedPodcast = (Podcast) updatedMediaItem;
        this.hasNewEpisodes = updatedPodcast.hasNewEpisodes;
        this.isDownloaded = updatedPodcast.isDownloaded;
        this.episodes.clear();
        this.episodes.addAll(updatedPodcast.episodes);
    }

    @Override
    public void syncWithDB() {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        String args[] = {String.valueOf(id), MediaListItem.TYPE_PODCAST};
        Cursor cursor = database.rawQuery("SELECT * FROM media_list WHERE media_id = ? AND media_type = ?", args);

        this.isDownloaded = false;
        this.isSubscribed = false;
        this.hasNewEpisodes = false;
        while (cursor.moveToNext()) {
            String listType = cursor.getString(cursor.getColumnIndex("list_type"));
            if (listType.equals(MediaListItem.NEW)) {
                this.hasNewEpisodes = true;
            } else if (listType.equals(MediaListItem.SUBSCRIBED)) {
                this.isSubscribed = true;
            } else if (listType.equals(MediaListItem.DOWNLOADED)) {
                this.isDownloaded = true;
            }
        }
        cursor.close();

        ArrayList<Episode> existingEpisodes = Episode.withPodcastId(id);
        episodes.clear();

        for (Episode episode : existingEpisodes) {
            Episode podcastEpisode = Episode.getEpisodByTitle(episodes, episode.getTitle());
            if (podcastEpisode == null) {
                episodes.add(episode);
            } else {
                podcastEpisode.isDownloaded = episode.isDownloaded;
                podcastEpisode.isNew = episode.isNew;
            }
        }

    }

    public static void syncWithDb(ArrayList<Podcast> podcasts) {
        ArrayList<MediaListItem> listItems = MediaListItem.withMediaType(MediaListItem.TYPE_PODCAST);
        for (MediaListItem listItem : listItems) {
            Podcast podcast = (Podcast) MediaItem.getMediaItemByName(podcasts, listItem.mediaItemName);
            if (podcast != null) {
                podcast.id = listItem.id;
                podcast.isExistsInDb = true;
                if (listItem.listType.equals(MediaListItem.SUBSCRIBED)) {
                    podcast.isSubscribed = true;
                } else if (listItem.listType.equals(MediaListItem.DOWNLOADED)) {
                    podcast.isDownloaded = true;
                } else if (listItem.listType.equals(MediaListItem.NEW)) {
                    podcast.hasNewEpisodes = true;
                }
                ArrayList<Episode> existingEpisodes = Episode.withPodcastId(podcast.id);
                for (Episode episode : existingEpisodes) {
                    Episode podcastEpisode = Episode.getEpisodByTitle(podcast.episodes, episode.getTitle());
                    if (podcastEpisode == null) {
                        podcast.episodes.add(episode);
                    } else {
                        podcastEpisode.isDownloaded = episode.isDownloaded;
                        podcastEpisode.isNew = episode.isNew;
                    }
                }
            }
        }
    }


    public static void syncNewEpisodes(ArrayList<Podcast> podcasts) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
        String args[] = {MediaListItem.TYPE_PODCAST, MediaListItem.NEW};
        Cursor cursor = database.rawQuery("SELECT p.id, p.name, m.list_type FROM podcasts as p " +
                "LEFT JOIN media_list as m ON p.id = m.media_id " +
                "WHERE m.media_type =  ? AND m.list_type =  ?", args);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex("id"));
            for (Podcast podcast : podcasts) {
                if (podcast.id == id) {
                    podcast.hasNewEpisodes = true;
                    ArrayList<Episode> existingEpisodes = Episode.withPodcastId(podcast.id);
                    podcast.getEpisodes().addAll(existingEpisodes);
                }
            }
        }
        cursor.close();
    }

    public static boolean hasEpisodeWithTitle(Podcast podcast, Episode episodeToCheck) {
        for (Episode episode : podcast.getEpisodes()) {
            if (GlobalUtils.safeTitleEquals(episode.getTitle(), episodeToCheck.getTitle())) {
                return true;
            }
        }
        return false;
    }
}
