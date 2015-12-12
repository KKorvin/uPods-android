package com.chickenkiller.upods2.models;

import android.content.Context;
import android.os.AsyncTask;

import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alonzilberman on 7/3/15.
 */
public class RadioItem extends MediaItem implements IPlayableMediaItem {

    private final static String DEFAULT_IMAGE = "https://upods.io/static/radio_stations/default/no_image.png";
    private final static String RADIO_LOG = "RADIO_LOG";
    private final static int MAX_URLS = 5;

    protected String name;
    protected Set<StreamUrl> streamUrls;
    protected String coverImageUrl;
    protected String bannerImageUrl;
    protected String description;
    protected String website;
    protected String facebook;
    protected String twitter;
    protected String country;
    protected String genre;

    public RadioItem(String name, StreamUrl streamUrl, String coverImageUrl) {
        this.name = name;
        this.streamUrls = new HashSet<>();
        this.streamUrls.add(streamUrl);
        this.coverImageUrl = coverImageUrl;
    }

    public RadioItem(RadioItem item) {
        this.name = item.name;
        this.streamUrls = new HashSet<>(item.streamUrls);
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
            this.streamUrls = new HashSet<>();
            this.id = jsonItem.has("id") ? jsonItem.getInt("id") : 0;
            this.name = jsonItem.has("name") ? jsonItem.getString("name") : "";
            this.description = jsonItem.has("description") ? jsonItem.getString("description") : "";
            this.website = jsonItem.has("website") ? jsonItem.getString("website") : "";
            this.facebook = jsonItem.has("facebook") ? jsonItem.getString("facebook") : "";
            this.twitter = jsonItem.has("twitter") ? jsonItem.getString("twitter") : "";
            this.country = jsonItem.has("country") ? jsonItem.getString("country") : "";

            if (!jsonItem.has("covers")) {//from profile
                this.coverImageUrl = jsonItem.has("cover_image_url") ? jsonItem.getString("cover_image_url") : "";
                this.bannerImageUrl = jsonItem.has("banner_image_url") ? jsonItem.getString("banner_image_url") : "";
                this.genre = jsonItem.has("genre") ? jsonItem.getString("genre") : "";
                if (jsonItem.has("streamUrls") && jsonItem.getJSONArray("streamUrls").length() > 0) {
                    JSONArray jsonStreamUrls = jsonItem.getJSONArray("streamUrls");
                    for (int i = 0; i < jsonStreamUrls.length(); i++) {
                        this.streamUrls.add(new StreamUrl(jsonStreamUrls.getJSONObject(i)));
                    }
                }
            } else {//from backend
                if (jsonItem.has("covers") && jsonItem.getJSONArray("covers").length() > 0) {
                    this.coverImageUrl = jsonItem.getJSONArray("covers").getString(0);
                }
                if (jsonItem.has("banners") && jsonItem.getJSONArray("banners").length() > 0) {
                    this.bannerImageUrl = jsonItem.getJSONArray("banners").getString(0);
                }
                if (jsonItem.has("genres") && jsonItem.getJSONArray("genres").length() > 0) {
                    this.genre = jsonItem.getJSONArray("genres").getString(0);
                }
                if (jsonItem.has("streamUrls") && jsonItem.getJSONArray("streamUrls").length() > 0) {
                    JSONArray jsonStreamUrls = jsonItem.getJSONArray("streamUrls");
                    for (int i = 0; i < jsonStreamUrls.length(); i++) {
                        this.streamUrls.add(new StreamUrl(jsonStreamUrls.getString(i)));
                    }
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
        return StreamUrl.getBestStreamUrl(streamUrls).getUrl();
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
            radioItem.put("cover_image_url", this.coverImageUrl);
            radioItem.put("banner_image_url", this.bannerImageUrl);
            radioItem.put("description", this.description);
            radioItem.put("website", this.website);
            radioItem.put("facebook", this.facebook);
            radioItem.put("twitter", this.twitter);
            radioItem.put("country", this.country);
            radioItem.put("genre", this.genre);
            JSONArray jsonStramUrls = new JSONArray();
            for (StreamUrl streamUrl : streamUrls) {
                jsonStramUrls.put(streamUrl.toJSON());
            }
            radioItem.put("streamUrls", jsonStramUrls);
        } catch (JSONException e) {
            Logger.printError(RADIO_LOG, "Can't convert radio to json");
            e.printStackTrace();
        }
        return radioItem;
    }


    /**
     * Fixes current RadioItem link (formail, isAlive) if needed.
     *
     * @param operationFinishSecsuessCallback
     * @param operationFinishFailCallback
     */
    public void fixAudeoLinks(final IOperationFinishCallback operationFinishSecsuessCallback, final IOperationFinishCallback operationFinishFailCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (StreamUrl streamUrl : streamUrls) {
                    final String currentUrl = streamUrl.getUrl();
                    if (GlobalUtils.isUrlReachable(currentUrl)) {
                        if (currentUrl.matches("(.+\\.m3u$)|(.+\\.pls$)")) {
                            MediaUtils.extractMp3FromFile(currentUrl, new IOperationFinishWithDataCallback() {
                                @Override
                                public void operationFinished(Object data) {
                                    StreamUrl.replaceUrl(streamUrls, (String) data, currentUrl);
                                    operationFinishSecsuessCallback.operationFinished();
                                }
                            });
                        } else {
                            operationFinishSecsuessCallback.operationFinished();
                        }
                        return;
                    } else {
                        streamUrl.isAlive = false;
                    }
                }
                operationFinishFailCallback.operationFinished();
            }
        });
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
