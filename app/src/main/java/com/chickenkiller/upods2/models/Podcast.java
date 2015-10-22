package com.chickenkiller.upods2.models;

import android.util.Log;

import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.ITrackable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 8/24/15.
 */
public class Podcast extends MediaItem implements IPlayableMediaItem, ITrackable {
    private static String PODCAST_LOG = "PODCAST";
    protected String name;
    protected String censoredName;
    protected String artistName;
    protected String feedUrl;
    protected String imageUrl;
    protected String releaseDate;
    protected String explicitness;
    protected String trackCount;
    protected String country;
    protected String genre;
    protected String description;

    protected ArrayList<Episod> episods;

    public Podcast() {
        super();
        this.episods = new ArrayList<>();
    }

    public Podcast(JSONObject jsonItem) {
        this();
        try {
            this.description = jsonItem.has("description") ? jsonItem.getString("description") : "";
            if (jsonItem.has("kind")) { //Itnes
                this.id = jsonItem.has("trackId") ? jsonItem.getInt("trackId") : 0;
                this.name = jsonItem.has("collectionName") ? jsonItem.getString("collectionName") : "";
                this.censoredName = jsonItem.has("collectionCensoredName") ? jsonItem.getString("collectionCensoredName") : "";
                this.artistName = jsonItem.has("artistName") ? jsonItem.getString("artistName") : "";
                this.feedUrl = jsonItem.has("feedUrl") ? jsonItem.getString("feedUrl") : "";
                this.imageUrl = jsonItem.has("artworkUrl100") ? jsonItem.getString("artworkUrl100") : "";
                if (this.imageUrl.isEmpty()) {
                    this.imageUrl = jsonItem.has("artworkUrl60") ? jsonItem.getString("artworkUrl60") : "";
                }
                this.country = jsonItem.has("country") ? jsonItem.getString("country") : "";
                this.releaseDate = jsonItem.has("releaseDate") ? jsonItem.getString("releaseDate") : "";
                this.explicitness = jsonItem.has("collectionExplicitness") ? jsonItem.getString("collectionExplicitness") : "";
                this.trackCount = jsonItem.has("trackCount") ? jsonItem.getString("trackCount") : "";
                this.genre = jsonItem.has("primaryGenreName") ? jsonItem.getString("primaryGenreName") : "";
            } else {//Our backend
                this.id = jsonItem.has("id") ? jsonItem.getInt("id") : 0;
                this.name = jsonItem.has("name") ? jsonItem.getString("name") : "";
                this.censoredName = jsonItem.has("censored_name") ? jsonItem.getString("censored_name") : "";
                this.artistName = jsonItem.has("artist_name") ? jsonItem.getString("artist_name") : "";
                this.feedUrl = jsonItem.has("feed_url") ? jsonItem.getString("feed_url") : "";
                this.imageUrl = jsonItem.has("image_url") ? jsonItem.getString("image_url") : "";
                this.country = jsonItem.has("country") ? jsonItem.getString("country") : "";
                this.releaseDate = jsonItem.has("release_date") ? jsonItem.getString("release_date") : "";
                this.explicitness = jsonItem.has("explicitness") ? jsonItem.getString("explicitness") : "";
                this.trackCount = jsonItem.has("track_count") ? jsonItem.getString("track_count") : "";
                if (jsonItem.has("genres") && jsonItem.getJSONArray("genres").length() > 0) {
                    this.genre = jsonItem.getJSONArray("genres").getString(0);
                }
                if (jsonItem.has("episods")) {
                    JSONArray jsonEpisods = jsonItem.getJSONArray("episods");
                    for (int i = 0; i < jsonEpisods.length(); i++) {
                        this.episods.add(new Episod(jsonEpisods.getJSONObject(i)));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(PODCAST_LOG, "Can't parse podcast from json");
            e.printStackTrace();
        }
    }

    public JSONObject toJSON(boolean convertEpisods) {
        JSONObject podcast = new JSONObject();
        try {
            podcast.put("id", this.id);
            podcast.put("name", this.name);
            podcast.put("censored_name", this.censoredName);
            podcast.put("artist_name", this.artistName);
            podcast.put("feed_url", this.feedUrl);
            podcast.put("image_url", this.imageUrl);
            podcast.put("country", this.country);
            podcast.put("release_date", this.releaseDate);
            podcast.put("explicitness", this.explicitness);
            podcast.put("track_count", this.trackCount);
            podcast.put("genre", this.genre);
            podcast.put("description", this.description);
            if (convertEpisods) {
                podcast.put("episods", Episod.toJSONArray(this.episods));
            }
        } catch (JSONException e) {
            Log.e(PODCAST_LOG, "Can't convert podcast to json");
            e.printStackTrace();
        }
        return podcast;
    }

    public static JSONArray toJsonArray(ArrayList<Podcast> podcasts, boolean convertEpisods) {
        JSONArray jsonPodcasts = new JSONArray();
        for (Podcast podcast : podcasts) {
            jsonPodcasts.put(podcast.toJSON(convertEpisods));
        }
        return jsonPodcasts;
    }

    public static boolean hasPodcastWithName(ArrayList<Podcast> podcasts, Podcast podcastToCheck) {
        for (Podcast podcast : podcasts) {
            if (podcast.getName().equals(podcastToCheck.getName())) {
                return true;
            }
        }
        return false;
    }

    public static Podcast getPodcastByName(ArrayList<Podcast> podcasts, Podcast podcastToget) {
        for (Podcast podcast : podcasts) {
            if (podcast.getName().equals(podcastToget.getName())) {
                return podcast;
            }
        }
        return null;
    }

    public Podcast(Podcast podcast) {
        this.name = podcast.getName();
        this.censoredName = podcast.getCensoredName();
        this.artistName = podcast.getArtistName();
        this.feedUrl = podcast.getFeedUrl();
        this.imageUrl = podcast.getCoverImageUrl();
        this.releaseDate = podcast.getReleaseDate();
        this.explicitness = podcast.getExplicitness();
        this.trackCount = podcast.getTrackCount();
        this.country = podcast.getCountry();
        this.genre = podcast.getGenre();
        this.episods = new ArrayList<Episod>(podcast.episods);
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
    public String getName() {
        return name;
    }

    @Override
    public String getCoverImageUrl() {
        return imageUrl;
    }

    @Override
    public String getSubHeader() {
        return this.genre;
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
        for (Episod episod : episods) {
            if (episod.isSelected) {
                return episod.getAudeoUrl();
            }
        }
        return episods.get(0).getAudeoUrl();
    }

    @Override
    public boolean hasTracks() {
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCensoredName() {
        return censoredName;
    }

    public void setCensoredName(String censoredName) {
        this.censoredName = censoredName;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(String trackCount) {
        this.trackCount = trackCount;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExplicitness() {
        return explicitness;
    }

    public void setExplicitness(String explicitness) {
        this.explicitness = explicitness;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public ArrayList<Episod> getEpisods() {
        return episods;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setTracks(ArrayList<? extends Track> tracks) {
        this.episods = (ArrayList<Episod>) tracks;
    }

    @Override
    public ArrayList<? extends Track> getTracks() {
        return this.episods;
    }

    @Override
    public String getTracksFeed() {
        return this.feedUrl;
    }

    @Override
    public void selectTrack(Track track) {
        for (Episod episod : episods) {
            episod.isSelected = episod.equals(track) ? true : false;
        }
    }


}
