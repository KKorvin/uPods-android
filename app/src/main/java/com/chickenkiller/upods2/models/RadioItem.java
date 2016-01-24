package com.chickenkiller.upods2.models;

import android.content.Context;
import android.os.AsyncTask;

import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.MediaUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
    protected StreamUrl selectedStreamUrl;
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
        this.selectedStreamUrl = null;
        this.streamUrls = new HashSet<>();
        this.streamUrls.add(streamUrl);
        this.coverImageUrl = coverImageUrl;
        this.bannerImageUrl = "";
        this.website = "";
        this.facebook = "";
        this.twitter = "";
        this.country = "";
        this.genre = "";
    }

    public RadioItem(RadioItem item) {
        this.name = item.name;
        this.name = this.name.replace("\n", "").trim();
        this.selectedStreamUrl = item.selectedStreamUrl;
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
            this.selectedStreamUrl = jsonItem.has("selectedStreamUrl") ? new StreamUrl(jsonItem.getJSONObject("selectedStreamUrl")) : null;

            if (jsonItem.has("streamUrls") && jsonItem.getJSONArray("streamUrls").length() > 0) {
                JSONArray jsonStreamUrls = jsonItem.getJSONArray("streamUrls");
                for (int i = 0; i < jsonStreamUrls.length(); i++) {
                    this.streamUrls.add(new StreamUrl(jsonStreamUrls.getJSONObject(i)));
                }
            }

            if (!jsonItem.has("covers")) {//from profile
                this.coverImageUrl = jsonItem.has("cover_image_url") ? jsonItem.getString("cover_image_url") : "";
                this.bannerImageUrl = jsonItem.has("banner_image_url") ? jsonItem.getString("banner_image_url") : "";
                this.genre = jsonItem.has("genre") ? jsonItem.getString("genre") : "";

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

            }
            this.name = this.name.replace("\n", "").trim();
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
        if (selectedStreamUrl != null) {
            return selectedStreamUrl.getUrl();
        }
        return StreamUrl.getBestStreamUrl(streamUrls).getUrl();
    }

    @Override
    public String getBitrate() {
        if (selectedStreamUrl != null) {
            return selectedStreamUrl.getBitrate();
        }
        return StreamUrl.getBestStreamUrl(streamUrls).getBitrate();
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
        return coverImageUrl != null && coverImageUrl.isEmpty() ? null : coverImageUrl;
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
            if (this.selectedStreamUrl != null) {
                radioItem.put("selectedStreamUrl", this.selectedStreamUrl.toJSON());
            }
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
                if (selectedStreamUrl != null) {
                    if (!GlobalUtils.isUrlReachable(selectedStreamUrl.getUrl())) {
                        selectedStreamUrl = null;
                    }
                }
                for (StreamUrl streamUrl : streamUrls) {
                    final String currentUrl = streamUrl.getUrl();
                    if (GlobalUtils.isUrlReachable(currentUrl)) {
                        if (currentUrl.matches("(.+\\.m3u$)|(.+\\.pls$)")) {
                            try {
                                String newUrl = MediaUtils.extractMp3FromFile(currentUrl);
                                StreamUrl.replaceUrl(streamUrls, newUrl, currentUrl);
                            } catch (Exception e) {
                                Logger.printInfo(RADIO_LOG, "Error with fetching radio url from file");
                                e.printStackTrace();
                                continue;
                            }
                        }
                        streamUrl.isAlive = true;
                        operationFinishSecsuessCallback.operationFinished();
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

    public String[] getAvailableStreams() {
        Set<String> availableStreams = StreamUrl.getAllAvailableStreams(streamUrls);
        List<String> list = new ArrayList<String>(availableStreams);
        Collections.sort(list, new Comparator<String>() {
            public int compare(String a, String b) {
                return Integer.parseInt(b) - Integer.parseInt(a);
            }
        });
        return list.toArray(new String[availableStreams.size()]);
    }

    public int getSelectedStreamAsNumber(String[] availableStreams) {
        if (selectedStreamUrl != null) {
            for (int i = 0; i < availableStreams.length; i++) {
                if (availableStreams[i].equals(selectedStreamUrl.getBitrate())) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < availableStreams.length; i++) {
                if (availableStreams[i].equals(getBitrate())) {
                    return i;
                }
            }
        }
        return 0;
    }

    public void selectStreamUrl(String quality) {
        for (StreamUrl streamUrl : streamUrls) {
            if (streamUrl.isAlive && streamUrl.hasBitrate() && streamUrl.getBitrate().equals(quality)) {
                selectedStreamUrl = streamUrl;
                return;
            }

        }

    }

    public boolean hasMultiplyStreams() {
        return getAvailableStreams().length > 1;
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
