package com.chickenkiller.upods2.controllers.app;

import com.chickenkiller.upods2.controllers.internet.SyncMaster;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.enums.MediaItemType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

/**
 * Created by alonzilberman on 9/28/15.
 */
public class ProfileManager {

    public static final String JS_DOWNLOADED_PODCASTS = "downloadedPodcasts";
    public static final String JS_SUBSCRIBED_PODCASTS = "subscribedPodcasts";
    public static final String JS_SUBSCRIBED_STATIONS = "subscribedStations";
    public static final String JS_RECENT_STATIONS = "recentStations";

    private static final String PROFILE = "PROFILE";
    private static final String PROFILE_PREF = "profile_pref";
    private static final int RECTNT_RADIO_STATIONS_LIMIT = 10;

    public static ProfileManager profileManager;
    private IOperationFinishCallback profileSavedCallback;


    private ProfileManager() {

    }

    public synchronized static ProfileManager getInstance() {
        if (profileManager == null) {
            profileManager = new ProfileManager();
        }
        return profileManager;
    }

    public void setProfileSavedCallback(IOperationFinishCallback profileSavedCallback) {
        this.profileSavedCallback = profileSavedCallback;
    }

    public ArrayList<Podcast> getDownloadedPodcasts() {
        ArrayList<Podcast> podcasts = new ArrayList<>();
        List<String> keys = Paper.book(JS_DOWNLOADED_PODCASTS).getAllKeys();
        for (String key : keys) {
            podcasts.add((Podcast) Paper.book(JS_DOWNLOADED_PODCASTS).read(key));
        }

        return podcasts;
    }

    public ArrayList<Podcast> getSubscribedPodcasts() {
        ArrayList<Podcast> podcasts = new ArrayList<>();
        List<String> keys = Paper.book(JS_SUBSCRIBED_PODCASTS).getAllKeys();
        for (String key : keys) {
            podcasts.add((Podcast) Paper.book(JS_SUBSCRIBED_PODCASTS).read(key));
        }

        return podcasts;
    }

    public ArrayList<RadioItem> getSubscribedRadioItems() {
        ArrayList<RadioItem> radioItems = new ArrayList<>();
        List<String> keys = Paper.book(JS_SUBSCRIBED_STATIONS).getAllKeys();
        for (String key : keys) {
            radioItems.add((RadioItem) Paper.book(JS_SUBSCRIBED_STATIONS).read(key));
        }
        return radioItems;
    }

    public ArrayList<RadioItem> getRecentRadioItems() {
        ArrayList<RadioItem> radioItems = new ArrayList<>();
        List<String> keys = Paper.book(JS_RECENT_STATIONS).getAllKeys();
        for (String key : keys) {
            radioItems.add((RadioItem) Paper.book(JS_RECENT_STATIONS).read(key));
        }
        return radioItems;
    }

    private void putMediaItemToDbIfNotExists(String bookName, String key, MediaItem mediaItem) {
        if (!Paper.book(bookName).exist(key)) {
            Paper.book(bookName).write(key, mediaItem);
        }
    }


    public void addSubscribedMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            String key = mediaItem.getName();
            putMediaItemToDbIfNotExists(JS_SUBSCRIBED_PODCASTS, key, mediaItem);
        } else if (mediaItem instanceof RadioItem) {
            String key = mediaItem.getName();
            putMediaItemToDbIfNotExists(JS_SUBSCRIBED_STATIONS, key, mediaItem);
        }
        notifyAboutChanges();
    }

    public void addRecentMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof RadioItem) {
            String key = mediaItem.getName();
            putMediaItemToDbIfNotExists(JS_RECENT_STATIONS, key, mediaItem);
        }
        notifyAboutChanges();
    }

    public void addDownloadedTrack(MediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episode) {
            String key = mediaItem.getName();
            if (!Paper.book(JS_DOWNLOADED_PODCASTS).exist(key)) {
                Podcast podcast = new Podcast((Podcast) mediaItem);
                podcast.getEpisodes().clear();
                podcast.getEpisodes().add((Episode) track);
                Paper.book(JS_DOWNLOADED_PODCASTS).write(key, podcast);
            } else {
                Podcast podcast = Paper.book(JS_DOWNLOADED_PODCASTS).read(key);
                podcast.getEpisodes().add((Episode) track);
                Paper.book(JS_DOWNLOADED_PODCASTS).write(key, podcast);
            }
        }
    }

    public void removeDownloadedTrack(MediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episode) {
            String key = mediaItem.getName();
            if (Paper.book(JS_DOWNLOADED_PODCASTS).exist(key)) {
                Podcast podcast = Paper.book(JS_DOWNLOADED_PODCASTS).read(key);
                if (podcast.getEpisodes().size() == 1) {
                    Paper.book(JS_DOWNLOADED_PODCASTS).delete(key);
                } else {
                    podcast.getEpisodes().remove(Episode.getEpisodByTitle(podcast.getEpisodes(), (Episode) track));
                    Paper.book(JS_DOWNLOADED_PODCASTS).write(key, podcast);
                }
            }
        }
    }

    public void markDownloadedTrackListened(MediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episode) {
            String key = mediaItem.getName();
            if (Paper.book(JS_DOWNLOADED_PODCASTS).exist(key)) {
                Podcast podcast = Paper.book(JS_DOWNLOADED_PODCASTS).read(key);
                if (podcast.getNewEpisodsTitles().contains(track.getTitle())) {
                    podcast.getNewEpisodsTitles().remove(track.getTitle());
                    ((Episode) track).isNotNew = true;
                    Paper.book(JS_DOWNLOADED_PODCASTS).write(key, podcast);
                }
            }
        }
    }

    public void removeDownloadedMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            String key = mediaItem.getName();
            Paper.book(JS_DOWNLOADED_PODCASTS).delete(key);
        }
        notifyAboutChanges();
    }

    public void removeRecentMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof RadioItem) {
            String key = mediaItem.getName();
            Paper.book(JS_RECENT_STATIONS).delete(key);
        }
        notifyAboutChanges();
    }

    public String getDownloadedMediaItemPath(MediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            String key = mediaItem.getName();
            if (Paper.book(JS_DOWNLOADED_PODCASTS).exist(key)) {
                Podcast podcast = Paper.book(JS_DOWNLOADED_PODCASTS).read(key);
                for (Episode episode : podcast.getEpisodes()) {
                    return episode.getAudeoUrl().replaceFirst("/.[^/]+mp3$", "");
                }
            }
        }
        return "";
    }

    public String getDownloadedTrackPath(MediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episode) {
            String key = mediaItem.getName();
            if (Paper.book(JS_DOWNLOADED_PODCASTS).exist(key)) {
                Podcast podcast = Paper.book(JS_DOWNLOADED_PODCASTS).read(key);
                for (Episode episode : podcast.getEpisodes()) {
                    if (episode.getTitle().equals(track.getTitle())) {
                        return episode.getAudeoUrl();
                    }
                }
            }
        }
        return "";
    }

    public boolean isDownloaded(MediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episode) {
            String key = mediaItem.getName();
            if (Paper.book(JS_DOWNLOADED_PODCASTS).exist(key)) {
                Podcast podcast = Paper.book(JS_DOWNLOADED_PODCASTS).read(key);
                return Episode.hasEpisodWithTitle(podcast.getEpisodes(), (Episode) track);
            }
        }
        return false;
    }

    public boolean isDownloaded(MediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            String key = mediaItem.getName();
            return Paper.book(JS_DOWNLOADED_PODCASTS).exist(key);
        }
        return false;
    }

    public boolean isSubscribedToMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            String key = mediaItem.getName();
            return Paper.book(JS_SUBSCRIBED_PODCASTS).exist(key);
        } else if (mediaItem instanceof RadioItem) {
            String key = mediaItem.getName();
            return Paper.book(JS_SUBSCRIBED_STATIONS).exist(key);
        }
        return false;
    }

    public boolean isRecentMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof RadioItem) {
            String key = mediaItem.getName();
            return Paper.book(JS_RECENT_STATIONS).exist(key);
        }
        return false;
    }

    public void removeSubscribedMediaItem(MediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            String key = mediaItem.getName();
            Paper.book(JS_SUBSCRIBED_PODCASTS).delete(key);
        } else if (mediaItem instanceof RadioItem) {
            String key = mediaItem.getName();
            Paper.book(JS_SUBSCRIBED_STATIONS).delete(key);
        }

    }

    public void replaceMediaItem(MediaItemType mediaItemType, MediaItem mediaItem) {
        if (mediaItemType == MediaItemType.PODCAST_FAVORITE) {
            if (mediaItem instanceof Podcast) {
                String key = mediaItem.getName();
                Paper.book(JS_SUBSCRIBED_PODCASTS).write(key, mediaItem);
            } else if (mediaItem instanceof RadioItem) {
                String key = mediaItem.getName();
                Paper.book(JS_SUBSCRIBED_STATIONS).write(key, mediaItem);
            }
        }
    }

    public void notifyAboutChanges() {
        if (LoginMaster.getInstance().isLogedIn()) {
            SyncMaster.saveToCloud();
        }
        if (profileSavedCallback != null) {
            profileSavedCallback.operationFinished();
        }
    }

    public void readFromJson(JSONObject rootProfile) {
        try {
            JSONArray subscribedPodcsts = rootProfile.getJSONArray(JS_SUBSCRIBED_PODCASTS);
            JSONArray subscribedStations = rootProfile.getJSONArray(JS_SUBSCRIBED_STATIONS);
            JSONArray recentRadio = rootProfile.getJSONArray(JS_RECENT_STATIONS);

            String key;
            for (int i = 0; i < subscribedPodcsts.length(); i++) {
                Podcast podcast = new Podcast(subscribedPodcsts.getJSONObject(i));
                key = podcast.getName();
                putMediaItemToDbIfNotExists(JS_SUBSCRIBED_PODCASTS, key, podcast);
            }

            for (int i = 0; i < subscribedStations.length(); i++) {
                RadioItem radioItem = new RadioItem(subscribedStations.getJSONObject(i));
                key = radioItem.getName();
                putMediaItemToDbIfNotExists(JS_SUBSCRIBED_STATIONS, key, radioItem);
            }

            for (int i = 0; i < recentRadio.length(); i++) {
                RadioItem radioItem = new RadioItem(recentRadio.getJSONObject(i));
                key = radioItem.getName();
                putMediaItemToDbIfNotExists(JS_RECENT_STATIONS, key, radioItem);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getAsJson() {
        JSONObject rootProfile = null;
        try {
            rootProfile = new JSONObject();
            JSONArray subscribedPodcasts = new JSONArray();
            JSONArray subscribedRadioStations = new JSONArray();
            JSONArray recentRadioStations = new JSONArray();

            for (RadioItem radioItem : getRecentRadioItems()) {
                recentRadioStations.put(radioItem.toJSON());
            }

            for (RadioItem radioItem : getSubscribedRadioItems()) {
                subscribedRadioStations.put(radioItem.toJSON());
            }

            for (Podcast podcast : getSubscribedPodcasts()) {
                subscribedPodcasts.put(podcast.toJSON(false));
            }

            rootProfile.put(JS_SUBSCRIBED_PODCASTS, subscribedPodcasts);
            rootProfile.put(JS_SUBSCRIBED_STATIONS, subscribedRadioStations);
            rootProfile.put(JS_RECENT_STATIONS, recentRadioStations);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return rootProfile;
    }

}
