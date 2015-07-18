package com.chickenkiller.upods2.models;

import android.content.Context;

import com.chickenkiller.upods2.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/3/15.
 */
public class RadioItem extends MediaItem {
    protected String name;
    protected String streamUrl;
    protected String coverImageUrl;
    protected String bannerImageUrl;
    protected String description;
    protected String website;
    protected String facebook;
    protected String twitter;
    protected String country;

    public RadioItem(String name, String url, String coverImageUrl) {
        this.name = name;
        this.streamUrl = url;
        this.coverImageUrl = coverImageUrl;
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
            if (jsonItem.has("covers")) {
                this.coverImageUrl = jsonItem.getJSONArray("covers").getString(0);
            }
            if (jsonItem.has("banners")) {
                this.bannerImageUrl = jsonItem.getJSONArray("banners").getString(0);
            }
            if (jsonItem.has("streamUrls")) {
                this.streamUrl = jsonItem.getJSONArray("streamUrls").getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<MediaItem> withJsonArray(JSONArray jsonRadioItems, Context mContext) {
        ArrayList<MediaItem> items = new ArrayList<MediaItem>();
        items.add(new BannersLayoutItem());
        items.add(new MediaItemTitle(mContext.getString(R.string.top40_chanels), mContext.getString(R.string.top40_chanels_subheader)));

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

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getDescription() {
        return description;
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
