package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.interfaces.IFeaturableMediaItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 8/24/15.
 */
public class Podcast extends MediaItem implements IFeaturableMediaItem{
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


    public Podcast(JSONObject jsonItem) {
        try {
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
            this.genre = jsonItem.has("genre") ? jsonItem.getString("genre") : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public String getCoverImageUrl() {
        return imageUrl;
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
}
