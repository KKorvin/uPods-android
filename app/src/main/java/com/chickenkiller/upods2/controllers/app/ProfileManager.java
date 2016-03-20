package com.chickenkiller.upods2.controllers.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chickenkiller.upods2.controllers.database.SQLdatabaseManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.models.Feed;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaListItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 9/28/15.
 */
public class ProfileManager {

    public static final String JS_DOWNLOADED_PODCASTS = "downloadedPodcasts";
    public static final String JS_SUBSCRIBED_PODCASTS = "subscribedPodcasts";
    public static final String JS_SUBSCRIBED_STATIONS = "subscribedStations";
    public static final String JS_RECENT_STATIONS = "recentStations";

    private static final int RECTNT_RADIO_STATIONS_LIMIT = 10;

    public static ProfileManager profileManager;
    private IOperationFinishWithDataCallback profileSavedCallback;


    public static class ProfileUpdateEvent {
        public String updateListType;
        public MediaItem mediaItem;
        public boolean isRemoved;

        public ProfileUpdateEvent(String updateListType, MediaItem mediaItem, boolean isRemoved) {
            this.updateListType = updateListType;
            this.mediaItem = mediaItem;
            this.isRemoved = isRemoved;
        }
    }

    private ProfileManager() {

    }

    public synchronized static ProfileManager getInstance() {
        if (profileManager == null) {
            profileManager = new ProfileManager();
        }
        return profileManager;
    }

    public void setProfileSavedCallback(IOperationFinishWithDataCallback profileSavedCallback) {
        this.profileSavedCallback = profileSavedCallback;
    }


    private ArrayList<Long> getMediaListIds(String mediaType, String listType) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
        String[] args1 = {mediaType, listType};
        ArrayList<Long> ids = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT p.id FROM podcasts as p\n" +
                "LEFT JOIN media_list as ml\n" +
                "ON p.id = ml.media_id\n" +
                "WHERE ml.media_type = ? and ml.list_type = ?", args1);
        while (cursor.moveToNext()) {
            ids.add(cursor.getLong(cursor.getColumnIndex("id")));
        }
        cursor.close();
        return ids;
    }

    private ArrayList<Podcast> getPodcastsForMediaType(String mediaType) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
        ArrayList<Long> ids = getMediaListIds(MediaListItem.TYPE_PODCAST,
                mediaType.equals(MediaListItem.DOWNLOADED) ? MediaListItem.SUBSCRIBED : MediaListItem.DOWNLOADED);
        ArrayList<Podcast> podcasts = new ArrayList<>();
        String[] args2 = {MediaListItem.TYPE_PODCAST, mediaType};
        Cursor cursor = database.rawQuery("SELECT p.* FROM podcasts as p\n" +
                "LEFT JOIN media_list as ml\n" +
                "ON p.id = ml.media_id\n" +
                "WHERE ml.media_type = ? and ml.list_type = ?", args2);
        while (cursor.moveToNext()) {
            Podcast podcast = Podcast.withCursor(cursor);
            if (mediaType.equals(MediaListItem.DOWNLOADED)) {
                podcast.isDownloaded = true;
                if (ids.contains(podcast.id)) {
                    podcast.isSubscribed = true;
                }
            } else if (mediaType.equals(MediaListItem.SUBSCRIBED)) {
                podcast.isSubscribed = true;
                if (ids.contains(podcast.id)) {
                    podcast.isDownloaded = true;
                }
            }
            podcasts.add(podcast);
        }
        cursor.close();
        return podcasts;
    }

    private ArrayList<RadioItem> getRadioStationsForMediaType(String mediaType) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
        ArrayList<Long> ids = getMediaListIds(MediaListItem.TYPE_RADIO,
                mediaType.equals(MediaListItem.RECENT) ? MediaListItem.SUBSCRIBED : MediaListItem.RECENT);
        ArrayList<RadioItem> radioItems = new ArrayList<>();
        String[] args2 = {MediaListItem.TYPE_RADIO, mediaType};

        Cursor cursor = database.rawQuery("SELECT r.* FROM radio_stations as r\n" +
                "LEFT JOIN media_list as ml\n" +
                "ON r.id = ml.media_id\n" +
                "WHERE ml.media_type = ? and ml.list_type = ?", args2);

        while (cursor.moveToNext()) {
            RadioItem radioItem = RadioItem.withCursor(cursor);
            if (mediaType.equals(MediaListItem.RECENT)) {
                radioItem.isRecent = true;
                if (ids.contains(radioItem.id)) {
                    radioItem.isSubscribed = true;
                }
            } else if (mediaType.equals(MediaListItem.SUBSCRIBED)) {
                radioItem.isSubscribed = true;
                if (ids.contains(radioItem.id)) {
                    radioItem.isRecent = true;
                }
            }
            radioItems.add(radioItem);
        }
        cursor.close();
        return radioItems;
    }

    private void addTrack(MediaItem mediaItem, Track track, String listType) {
        if (mediaItem instanceof Podcast && track instanceof Episode) {
            Podcast podcast = (Podcast) mediaItem;
            Episode episode = (Episode) track;

            if (listType.equals(MediaListItem.DOWNLOADED)) {
                episode.isDownloaded = true;
                podcast.isDownloaded = true;
            } else if (listType.equals(MediaListItem.NEW)) {
                episode.isNew = true;
                podcast.hasNewEpisodes = true;
                Logger.printInfo("OLOLOLOOLOL", "EPISODE!!!");
            }

            SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
            if (!podcast.isExistsInDb) {
                podcast.save(); //If podcast doesn't exists save() will save it and all his new or downloaded episodes
                ContentValues values = new ContentValues();
                values.put("media_id", mediaItem.id);
                values.put("media_type", MediaListItem.TYPE_PODCAST);
                values.put("list_type", listType);
                database.insert("media_list", null, values);
            }
            if (!episode.isExistsInDb) {
                episode.save(podcast.id); //If episode doesn't exists save() will both save it and create podcasts_episodes_rel
            } else {
                ContentValues values = new ContentValues();
                values.put("podcast_id", mediaItem.id);
                values.put("episode_id", episode.id);
                values.put("type", listType);
                database.insert("podcasts_episodes_rel", null, values);
            }
            notifyChanges(new ProfileUpdateEvent(MediaListItem.DOWNLOADED, mediaItem, false));
        }
    }

    private void removeTrack(MediaItem mediaItem, Track track, String listType) {
        if (mediaItem instanceof Podcast && track instanceof Episode) {
            SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
            Podcast podcast = (Podcast) mediaItem;
            Episode episode = (Episode) track;
            String type = Episode.DOWNLOADED;

            if (listType.equals(MediaListItem.DOWNLOADED)) {
                episode.isDownloaded = false;
            } else if (listType.equals(MediaListItem.NEW)) {
                episode.isNew = false;
                type = Episode.NEW;
            }

            String args[] = {String.valueOf(podcast.id), String.valueOf(episode.id), type};
            database.delete("podcasts_episodes_rel", "podcast_id = ? AND episode_id = ? AND type = ?", args);

            if (listType.equals(MediaListItem.DOWNLOADED) && podcast.getDownloadedEpisodsCount() == 0) {
                String args2[] = {String.valueOf(podcast.id), MediaListItem.TYPE_PODCAST, MediaListItem.DOWNLOADED};
                database.delete("media_list", "media_id = ? AND media_type = ? AND list_type = ?", args2);
                podcast.isDownloaded = false;
                notifyChanges(new ProfileUpdateEvent(MediaListItem.DOWNLOADED, mediaItem, true));
            } else if (listType.equals(MediaListItem.NEW) && podcast.getDownloadedEpisodsCount() == 0) {
                String args2[] = {String.valueOf(podcast.id), MediaListItem.TYPE_PODCAST, MediaListItem.NEW};
                database.delete("media_list", "media_id = ? AND media_type = ? AND list_type = ?", args2);
            }
        }
    }

    public ArrayList<Podcast> getDownloadedPodcasts() {
        return getPodcastsForMediaType(MediaListItem.DOWNLOADED);
    }

    public ArrayList<Podcast> getSubscribedPodcasts() {
        return getPodcastsForMediaType(MediaListItem.SUBSCRIBED);
    }

    public ArrayList<RadioItem> getSubscribedRadioItems() {
        return getRadioStationsForMediaType(MediaListItem.SUBSCRIBED);
    }

    public ArrayList<RadioItem> getRecentRadioItems() {
        return getRadioStationsForMediaType(MediaListItem.RECENT);
    }


    public void addSubscribedMediaItem(MediaItem mediaItem) {
        if (!mediaItem.isSubscribed) {
            String mediaType = MediaListItem.TYPE_RADIO;
            if (mediaItem instanceof Podcast) {
                mediaType = MediaListItem.TYPE_PODCAST;
                Podcast podcast = ((Podcast) mediaItem);
                if (!mediaItem.isExistsInDb) {
                    podcast.save();
                }
                Feed.saveAsFeed(podcast.getFeedUrl(), podcast.getEpisodes(), true);

            } else if (mediaItem instanceof RadioItem) {
                if (!mediaItem.isExistsInDb) {
                    ((RadioItem) mediaItem).save();
                }
            }
            ContentValues values = new ContentValues();
            values.put("media_id", mediaItem.id);
            values.put("media_type", mediaType);
            values.put("list_type", MediaListItem.SUBSCRIBED);
            UpodsApplication.getDatabaseManager().getWritableDatabase().insert("media_list", null, values);
            mediaItem.isSubscribed = true;
        }
        notifyChanges(new ProfileUpdateEvent(MediaListItem.SUBSCRIBED, mediaItem, false));
    }

    public void addRecentMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof RadioItem) {
            if (!((RadioItem) mediaItem).isRecent) {
                if (!mediaItem.isExistsInDb) {
                    ((RadioItem) mediaItem).save();
                }
                ContentValues values = new ContentValues();
                values.put("media_id", mediaItem.id);
                values.put("media_type", MediaListItem.TYPE_RADIO);
                values.put("list_type", MediaListItem.RECENT);
                UpodsApplication.getDatabaseManager().getWritableDatabase().insert("media_list", null, values);
                ((RadioItem) mediaItem).isRecent = true;
            }
        }
        notifyChanges(new ProfileUpdateEvent(MediaListItem.RECENT, mediaItem, false));
    }

    public void addNewTrack(MediaItem mediaItem, Track track) {
        addTrack(mediaItem, track, MediaListItem.NEW);
    }

    public void addDownloadedTrack(MediaItem mediaItem, Track track) {
        addTrack(mediaItem, track, MediaListItem.DOWNLOADED);
    }

    public void removeNewTrack(MediaItem mediaItem, Track track) {
        removeTrack(mediaItem, track, MediaListItem.NEW);
    }

    public void removeDownloadedTrack(MediaItem mediaItem, Track track) {
        removeTrack(mediaItem, track, MediaListItem.DOWNLOADED);
    }

    public void removeRecentMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof RadioItem) {
            SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
            String args[] = {String.valueOf(mediaItem.id), MediaListItem.TYPE_RADIO, MediaListItem.RECENT};
            database.delete("media_list", "media_id = ? AND media_type = ? AND list_type = ?", args);
            ((RadioItem) mediaItem).isRecent = false;
        }
        notifyChanges(new ProfileUpdateEvent(MediaListItem.SUBSCRIBED, mediaItem, true));
    }

    public void removeSubscribedMediaItem(MediaItem mediaItem) {
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
        String type = MediaListItem.TYPE_RADIO;
        if (mediaItem instanceof Podcast) {
            type = MediaListItem.TYPE_PODCAST;
        }
        mediaItem.isSubscribed = false;
        String args[] = {String.valueOf(mediaItem.id), type, MediaListItem.SUBSCRIBED};
        database.delete("media_list", "media_id = ? AND media_type = ? AND list_type = ?", args);
        notifyChanges(new ProfileUpdateEvent(MediaListItem.SUBSCRIBED, mediaItem, true));
    }

    public void notifyChanges(ProfileUpdateEvent updateEvent) {
        if (profileSavedCallback != null) {
            profileSavedCallback.operationFinished(updateEvent);
        }
    }

    private void initSubscribedPodcasts(JSONArray jSubscribedPodcasts) {
        try {
            SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
            ArrayList<Podcast> subscribedPodcasts = new ArrayList<>();
            for (int i = 0; i < jSubscribedPodcasts.length(); i++) {
                Podcast podcast = new Podcast(jSubscribedPodcasts.getJSONObject(i));
                subscribedPodcasts.add(podcast);
            }
            Podcast.syncWithDb(subscribedPodcasts);
            for (Podcast podcast : subscribedPodcasts) {
                if (!podcast.isExistsInDb) {
                    addSubscribedMediaItem(podcast);
                }
            }
            ArrayList<String> args = new ArrayList<>();
            args.add(MediaListItem.TYPE_PODCAST);
            args.add(MediaListItem.SUBSCRIBED);
            args.addAll(MediaItem.getIds(subscribedPodcasts));
            database.delete("media_list", "media_type = ? AND list_type = ? AND media_id in " + SQLdatabaseManager.makePlaceholders(args.size()),
                    (String[]) args.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRadioStations(JSONArray jStations, String listType) {
        try {
            SQLiteDatabase database = UpodsApplication.getDatabaseManager().getWritableDatabase();
            ArrayList<RadioItem> radioStations = new ArrayList<>();
            for (int i = 0; i < jStations.length(); i++) {
                RadioItem radioItem = new RadioItem(jStations.getJSONObject(i));
                radioStations.add(radioItem);
            }
            RadioItem.syncWithDb(radioStations);
            for (RadioItem radioItem : radioStations) {
                if (!radioItem.isExistsInDb) {
                    if (listType.equals(MediaListItem.SUBSCRIBED)) {
                        addSubscribedMediaItem(radioItem);
                    } else {
                        addRecentMediaItem(radioItem);
                    }
                }
            }
            ArrayList<String> args = new ArrayList<>();
            args.add(MediaListItem.TYPE_RADIO);
            args.add(listType);
            args.addAll(MediaItem.getIds(radioStations));
            database.delete("media_list", "media_type = ? AND list_type = ? AND media_id in " + SQLdatabaseManager.makePlaceholders(args.size()),
                    (String[]) args.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readFromJson(JSONObject rootProfile) {
        try {
            //Read from JSON -> syncWithDB -> save all which not exists -> remove all which not in JSON

            initSubscribedPodcasts(rootProfile.getJSONArray(JS_SUBSCRIBED_PODCASTS));
            initRadioStations(rootProfile.getJSONArray(JS_SUBSCRIBED_STATIONS), MediaListItem.SUBSCRIBED);
            initRadioStations(rootProfile.getJSONArray(JS_RECENT_STATIONS), MediaListItem.RECENT);

            //TODO notify UI here

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public JSONObject getAsJson() {
        JSONObject rootProfile = new JSONObject();
        try {
            rootProfile.put(JS_RECENT_STATIONS, RadioItem.toJsonArray(getRecentRadioItems()));
            rootProfile.put(JS_SUBSCRIBED_STATIONS, RadioItem.toJsonArray(getSubscribedRadioItems()));
            rootProfile.put(JS_SUBSCRIBED_PODCASTS, Podcast.toJsonArray(getSubscribedPodcasts()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rootProfile;
    }

}
