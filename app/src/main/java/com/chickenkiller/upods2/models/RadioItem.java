package com.chickenkiller.upods2.models;

import android.content.Context;
import android.util.Log;

import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.utils.GlobalUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/3/15.
 */
public class RadioItem extends MediaItem implements IPlayableMediaItem {

    private final static String DEFAULT_IMAGE = "https://upods.io/static/radio_stations/default/no_image.png";
    private static final String RADIO_LOG = "RADIO_LOG";

    protected String name;
    protected String streamUrl;
    protected String coverImageUrl;
    protected String bannerImageUrl;
    protected String description;
    protected String website;
    protected String facebook;
    protected String twitter;
    protected String country;
    protected String genre;

    public RadioItem(String name, String url, String coverImageUrl) {
        this.name = name;
        this.streamUrl = url;
        this.coverImageUrl = coverImageUrl;
    }

    public RadioItem(RadioItem item) {
        this.name = item.name;
        this.streamUrl = item.streamUrl;
        this.coverImageUrl = item.coverImageUrl;
        this.bannerImageUrl = item.bannerImageUrl;
        this.description = item.description;
        this.website = item.website;
        this.facebook = item.facebook;
        this.twitter = item.twitter;
        this.country = item.country;
    }

    public RadioItem(JSONObject jsonItem) {
        try {
            this.id = jsonItem.has("id") ? jsonItem.getInt("id") : 0;
            this.name = jsonItem.has("name") ? jsonItem.getString("name") : "";
            this.description = jsonItem.has("description") ? jsonItem.getString("description") : "";
            this.website = jsonItem.has("website") ? jsonItem.getString("website") : "";
            this.facebook = jsonItem.has("facebook") ? jsonItem.getString("facebook") : "";
            this.twitter = jsonItem.has("twitter") ? jsonItem.getString("twitter") : "";
            this.country = jsonItem.has("country") ? jsonItem.getString("country") : "";

            if (jsonItem.has("stream_url")) {//from profiel
                this.coverImageUrl = jsonItem.has("cover_image_url") ? jsonItem.getString("cover_image_url") : "";
                this.bannerImageUrl = jsonItem.has("banner_image_url") ? jsonItem.getString("banner_image_url") : "";
                this.streamUrl = jsonItem.has("stream_url") ? jsonItem.getString("stream_url") : "";
                this.genre = jsonItem.has("genre") ? jsonItem.getString("genre") : "";
            } else {//from backend
                if (jsonItem.has("covers") && jsonItem.getJSONArray("covers").length() > 0) {
                    this.coverImageUrl = jsonItem.getJSONArray("covers").getString(0);
                }
                if (jsonItem.has("banners") && jsonItem.getJSONArray("banners").length() > 0) {
                    this.bannerImageUrl = jsonItem.getJSONArray("banners").getString(0);
                }
                if (jsonItem.has("streamUrls") && jsonItem.getJSONArray("streamUrls").length() > 0) {
                    this.streamUrl = GlobalUtils.getBestStreamUrl(jsonItem.getJSONArray("streamUrls"));
                }
                if (jsonItem.has("genres") && jsonItem.getJSONArray("genres").length() > 0) {
                    this.genre = jsonItem.getJSONArray("genres").getString(0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<MediaItem> withOnlyBannersHeader() {
        ArrayList<MediaItem> items = new ArrayList<MediaItem>();
        items.add(new BannersLayoutItem());
        return items;
    }

    public static ArrayList<RadioItem> withJsonArray(JSONArray jsonRadioItems, Context mContext) {
        ArrayList<RadioItem> items = new ArrayList<RadioItem>();
        try {
            for (int i = 0; i < jsonRadioItems.length(); i++) {
                JSONObject jsonRadionItem = (JSONObject) jsonRadioItems.get(i);
                items.add(new RadioItem(jsonRadionItem));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public String getAudeoLink() {
        return streamUrl;
    }

    @Override
    public boolean hasTracks() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public JSONObject toJSON() {
        JSONObject radioItem = new JSONObject();
        try {
            radioItem.put("id", this.id);
            radioItem.put("name", this.name);
            radioItem.put("stream_url", this.streamUrl);
            radioItem.put("cover_image_url", this.coverImageUrl);
            radioItem.put("banner_image_url", this.bannerImageUrl);
            radioItem.put("description", this.description);
            radioItem.put("website", this.website);
            radioItem.put("facebook", this.facebook);
            radioItem.put("twitter", this.twitter);
            radioItem.put("country", this.country);
            radioItem.put("genre", this.genre);
        } catch (JSONException e) {
            Log.e(RADIO_LOG, "Can't convert radio to json");
            e.printStackTrace();
        }
        return radioItem;
    }

    public static JSONArray toJsonArray(ArrayList<RadioItem> radioItems) {
        JSONArray jsonRadioItems = new JSONArray();
        for (RadioItem radioItem : radioItems) {
            jsonRadioItems.put(radioItem.toJSON());
        }
        return jsonRadioItems;
    }


    @Override
    public String getSubHeader() {
        return this.country;
    }

    @Override
    public String getBottomHeader() {
        return null;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAudeoLink(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }
}
