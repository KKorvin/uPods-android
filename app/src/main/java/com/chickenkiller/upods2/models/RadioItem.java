package com.chickenkiller.upods2.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.interfaces.IMediaItemView;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
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
public class RadioItem extends MediaItem {

    public static final String TABLE = "radio_stations";
    private final static String RADIO_LOG = "RADIO_LOG";

    protected Set<StreamUrl> streamUrls;
    protected StreamUrl selectedStreamUrl;
    protected String bannerImageUrl;
    protected String description;
    protected String website;
    protected String facebook;
    protected String twitter;
    protected String country;
    protected String genre;

    //Not in DB
    public boolean isRecent;

    public RadioItem() {
        super();
        this.name = "";
        this.description = "";
        this.selectedStreamUrl = null;
        this.streamUrls = new HashSet<>();
        this.coverImageUrl = "";
        this.bannerImageUrl = "";
        this.website = "";
        this.facebook = "";
        this.twitter = "";
        this.country = "";
        this.genre = "";
    }

    public RadioItem(String name, StreamUrl streamUrl, String coverImageUrl) {
        this();
        this.name = name;
        this.name = this.name.replace("\n", "").trim();
        this.streamUrls.add(streamUrl);
        this.coverImageUrl = coverImageUrl;
    }

    public RadioItem(RadioItem item) {
        this.id = item.id;
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
        this.isExistsInDb = item.isExistsInDb;
        this.isRecent = item.isRecent;
        this.isSubscribed = item.isSubscribed;
    }

    public long save() {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("website", website);
        values.put("facebook", facebook);
        values.put("twitter", twitter);
        values.put("cover_image_url", coverImageUrl);
        values.put("country", country);
        values.put("genre", genre);

        id = database.insert(TABLE, null, values);
        isExistsInDb = true;
        StreamUrl.saveSet(streamUrls, id);

        return id;
    }

    public RadioItem(JSONObject jsonItem) {
        try {
            this.streamUrls = new HashSet<>();
            this.id = jsonItem.has("id") ? jsonItem.getInt("id") : 0;
            this.name = jsonItem.has("name") ? jsonItem.getString("name") : "";
            this.name = name.replace("\n", "").trim();
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
    public String getName() {
        return name;
    }

    @Override
    public String getCoverImageUrl() {
        return coverImageUrl != null && coverImageUrl.isEmpty() ? null : coverImageUrl;
    }

    @Override
    public String getSubHeader() {
        return this.country;
    }

    @Override
    public String getDescription() {
        return description != null ? description : "";
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCountry() {
        return country;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
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
                boolean hasAtleastOneUrl = false;
                if (selectedStreamUrl != null) {
                    if (!GlobalUtils.isUrlReachable(selectedStreamUrl.getUrl())) {
                        selectedStreamUrl = null;
                    } else {
                        operationFinishSecsuessCallback.operationFinished();
                        return;
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
                        hasAtleastOneUrl = true;
                        streamUrl.isAlive = true;
                    } else {
                        streamUrl.isAlive = false;
                    }
                }
                if (hasAtleastOneUrl) {
                    operationFinishSecsuessCallback.operationFinished();
                } else {
                    operationFinishFailCallback.operationFinished();
                }
            }
        });
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
    public void syncWithDB() {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        String args[] = {String.valueOf(id), MediaListItem.TYPE_RADIO};
        Cursor cursor = database.rawQuery("SELECT * FROM media_list WHERE media_id = ? AND media_type = ?", args);

        this.isSubscribed = false;
        this.isRecent = false;
        while (cursor.moveToNext()) {
            String listType = cursor.getString(cursor.getColumnIndex("list_type"));
            if (listType.equals(MediaListItem.RECENT)) {
                this.isRecent = true;
            } else if (listType.equals(MediaListItem.SUBSCRIBED)) {
                this.isSubscribed = true;
            }
        }
    }

    @Override
    public void syncWithMediaItem(MediaItem updatedMediaItem) {
        super.syncWithMediaItem(updatedMediaItem);
        this.isRecent = ((RadioItem) updatedMediaItem).isRecent;
    }

    public static JSONArray toJsonArray(ArrayList<RadioItem> radioItems) {
        JSONArray jsonRadioItems = new JSONArray();
        for (RadioItem radioItem : radioItems) {
            jsonRadioItems.put(radioItem.toJSON());
        }
        return jsonRadioItems;
    }

    public static ArrayList<IMediaItemView> withOnlyBannersHeader() {
        ArrayList<IMediaItemView> items = new ArrayList<IMediaItemView>();
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

    public static void syncWithDb(ArrayList<RadioItem> radioItems) {
        ArrayList<MediaListItem> listItems = MediaListItem.withMediaType(MediaListItem.TYPE_RADIO);
        for (MediaListItem listItem : listItems) {
            MediaItem mediaItem = MediaItem.getMediaItemByName(radioItems, listItem.mediaItemName);
            if (mediaItem != null) {
                mediaItem.id = listItem.id;
                mediaItem.isExistsInDb = true;
                if (listItem.listType.equals(MediaListItem.SUBSCRIBED)) {
                    mediaItem.isSubscribed = true;
                } else if (listItem.listType.equals(MediaListItem.RECENT)) {
                    ((RadioItem) mediaItem).isRecent = true;
                }
            }
        }
    }

    public static RadioItem withCursor(Cursor cursor) {
        RadioItem radioItem = new RadioItem();
        radioItem.isExistsInDb = true;
        radioItem.id = cursor.getLong(cursor.getColumnIndex("id"));
        radioItem.name = cursor.getString(cursor.getColumnIndex("name"));
        radioItem.website = cursor.getString(cursor.getColumnIndex("website"));
        radioItem.facebook = cursor.getString(cursor.getColumnIndex("facebook"));
        radioItem.description = cursor.getString(cursor.getColumnIndex("description"));
        radioItem.twitter = cursor.getString(cursor.getColumnIndex("twitter"));
        radioItem.coverImageUrl = cursor.getString(cursor.getColumnIndex("cover_image_url"));
        radioItem.country = cursor.getString(cursor.getColumnIndex("country"));
        radioItem.genre = cursor.getString(cursor.getColumnIndex("genre"));

        radioItem.streamUrls.addAll(StreamUrl.withRaioItemId(radioItem.id));

        return radioItem;
    }
}
