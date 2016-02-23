package com.chickenkiller.upods2.controllers.app;

import android.os.Handler;

import com.chickenkiller.upods2.controllers.internet.SyncMaster;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.models.Episod;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.Logger;
import com.pixplicity.easyprefs.library.Prefs;

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

    private static final String PROFILE = "PROFILE";
    private static final String PROFILE_PREF = "profile_pref";
    private static final int RECTNT_RADIO_STATIONS_LIMIT = 10;

    public enum ProfileItem {DOWNLOADED_PODCASTS, SUBSCRIBDED_PODCASTS, SUBSCRIBDED_RADIO, RECENT_RADIO, SUBSCRIBED_STATIONS}

    public static ProfileManager profileManager;
    private IOperationFinishCallback profileSavedCallback;

    private ArrayList<Podcast> downloadedPodcasts;
    private ArrayList<Podcast> subscribedPodcasts;
    private ArrayList<RadioItem> subscribedRadioItems;
    private ArrayList<RadioItem> recentRadioItems;

    private ProfileManager() {
        this.downloadedPodcasts = new ArrayList<>();
        this.subscribedPodcasts = new ArrayList<>();
        this.subscribedRadioItems = new ArrayList<>();
        this.recentRadioItems = new ArrayList<>();
        String profileJsonStr = Prefs.getString(PROFILE_PREF, null);
        if (profileJsonStr == null) {
            JSONObject rootProfile = new JSONObject();
            JSONArray downloadedPodcasts = new JSONArray();
            JSONArray subscribedPodcasts = new JSONArray();
            JSONArray subscribedRadioStations = new JSONArray();
            JSONArray recentRadioStations = new JSONArray();
            try {
                rootProfile.put(JS_DOWNLOADED_PODCASTS, downloadedPodcasts);
                rootProfile.put(JS_SUBSCRIBED_PODCASTS, subscribedPodcasts);
                rootProfile.put(JS_SUBSCRIBED_STATIONS, subscribedRadioStations);
                rootProfile.put(JS_RECENT_STATIONS, recentRadioStations);
            } catch (JSONException e) {
                Logger.printError(PROFILE, "Can't create empty profile object");
                e.printStackTrace();
            }
            profileJsonStr = rootProfile.toString();
            Prefs.putString(PROFILE_PREF, profileJsonStr);
        }
        try {
            readFromJson(new JSONObject(profileJsonStr));
        } catch (JSONException e) {
            Logger.printError(PROFILE, "Can't parse profile string to json: " + profileJsonStr);
            e.printStackTrace();
        }
    }

    public static ProfileManager getInstance() {
        if (profileManager == null) {
            profileManager = new ProfileManager();
        }
        return profileManager;
    }

    public void setProfileSavedCallback(IOperationFinishCallback profileSavedCallback) {
        this.profileSavedCallback = profileSavedCallback;
    }

    public ArrayList<Podcast> getDownloadedPodcasts() {
        return this.downloadedPodcasts;
    }

    public ArrayList<Podcast> getSubscribedPodcasts() {
        return this.subscribedPodcasts;
    }

    public ArrayList<RadioItem> getSubscribedRadioItems() {
        return this.subscribedRadioItems;
    }

    public ArrayList<RadioItem> getRecentRadioItems() {
        return this.recentRadioItems;
    }


    private void initProfilePodcastItem(JSONArray podcasts, ProfileItem profileItem) {
        for (int i = 0; i < podcasts.length(); i++) {
            try {
                Podcast podcast = new Podcast(podcasts.getJSONObject(i));
                if (profileItem == ProfileItem.DOWNLOADED_PODCASTS) {
                    if (!MediaItem.hasMediaItemWithName(downloadedPodcasts, podcast)) {
                        downloadedPodcasts.add(podcast);
                    }
                } else if (profileItem == ProfileItem.SUBSCRIBDED_PODCASTS) {
                    if (!MediaItem.hasMediaItemWithName(subscribedPodcasts, podcast)) {
                        subscribedPodcasts.add(podcast);
                    }
                }
            } catch (JSONException e) {
                Logger.printError(PROFILE, "Can't fetch podcast from json object at index" + String.valueOf(i));
                e.printStackTrace();
            }
        }
        String podcastType = "";
        if (profileItem == ProfileItem.DOWNLOADED_PODCASTS) {
            podcastType = " downloaded ";
        } else if (profileItem == ProfileItem.SUBSCRIBDED_PODCASTS) {
            podcastType = " subscribded ";
        }
        Logger.printInfo(PROFILE, "Fetcheed " + String.valueOf(podcasts.length()) + podcastType + "podcasts from json profile");
    }

    private void initProfileRadioItems(JSONArray radioItems, ProfileItem profileItem) {
        for (int i = 0; i < radioItems.length(); i++) {
            try {
                RadioItem radioItem = new RadioItem(radioItems.getJSONObject(i));
                if (profileItem == ProfileItem.SUBSCRIBDED_RADIO) {
                    if (!MediaItem.hasMediaItemWithName(subscribedRadioItems, radioItem)) {
                        subscribedRadioItems.add(radioItem);
                    }
                } else if (profileItem == ProfileItem.RECENT_RADIO) {
                    if (!MediaItem.hasMediaItemWithName(recentRadioItems, radioItem)) {
                        recentRadioItems.add(radioItem);
                    }
                }
            } catch (JSONException e) {
                Logger.printError(PROFILE, "Can't fetch radio item from json object at index" + String.valueOf(i));
                e.printStackTrace();
            }
        }
        String podcastType = "";
        if (profileItem == ProfileItem.SUBSCRIBDED_RADIO) {
            podcastType = " subscribded ";
        } else if (profileItem == ProfileItem.RECENT_RADIO) {
            podcastType = " recent ";
        }
        Logger.printInfo(PROFILE, "Fetcheed " + String.valueOf(radioItems.length()) + podcastType + "radio items from json profile");
    }

    public void addSubscribedMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            if (!MediaItem.hasMediaItemWithName(subscribedPodcasts, mediaItem)) {
                subscribedPodcasts.add((Podcast) mediaItem);
                saveChanges(ProfileItem.SUBSCRIBDED_PODCASTS);
            }
        } else if (mediaItem instanceof RadioItem) {
            if (!MediaItem.hasMediaItemWithName(subscribedRadioItems, mediaItem)) {
                subscribedRadioItems.add((RadioItem) mediaItem);
                saveChanges(ProfileItem.SUBSCRIBDED_RADIO);
            }
        }
    }

    public void addRecentMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof RadioItem) {
            if (!MediaItem.hasMediaItemWithName(recentRadioItems, mediaItem)) {
                if (recentRadioItems.size() == RECTNT_RADIO_STATIONS_LIMIT) {
                    recentRadioItems.remove(recentRadioItems.size() - 1);
                }
                recentRadioItems.add(0, (RadioItem) mediaItem);
                saveChanges(ProfileItem.RECENT_RADIO, false);
            }
        }
    }

    public void addDownloadedTrack(IPlayableMediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episod) {
            if (!MediaItem.hasMediaItemWithName(downloadedPodcasts, mediaItem)) {
                Podcast podcast = new Podcast((Podcast) mediaItem);
                podcast.getEpisods().clear();
                podcast.getEpisods().add((Episod) track);
                downloadedPodcasts.add(podcast);
            } else {
                Podcast podcast = (Podcast) MediaItem.getMediaItemByName(downloadedPodcasts, mediaItem);
                podcast.getEpisods().add((Episod) track);
            }
            saveChanges(ProfileItem.DOWNLOADED_PODCASTS);
        }
    }

    public void removeDownloadedTrack(IPlayableMediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episod) {
            if (MediaItem.hasMediaItemWithName(downloadedPodcasts, mediaItem)) {
                Podcast podcast = (Podcast) MediaItem.getMediaItemByName(downloadedPodcasts, mediaItem);
                if (podcast.getEpisods().size() == 1) {
                    downloadedPodcasts.remove(podcast);
                } else {
                    podcast.getEpisods().remove(Episod.getEpisodByTitle(podcast.getEpisods(), (Episod) track));
                }
                saveChanges(ProfileItem.DOWNLOADED_PODCASTS);
            }
        }
    }

    public void removeDownloadedMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            if (MediaItem.hasMediaItemWithName(downloadedPodcasts, mediaItem)) {
                Podcast podcast = (Podcast) MediaItem.getMediaItemByName(downloadedPodcasts, mediaItem);
                downloadedPodcasts.remove(podcast);
                saveChanges(ProfileItem.DOWNLOADED_PODCASTS);
            }
        }
    }

    public void removeRecentMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof RadioItem) {
            if (MediaItem.hasMediaItemWithName(recentRadioItems, mediaItem)) {
                RadioItem radioItem = (RadioItem) MediaItem.getMediaItemByName(recentRadioItems, mediaItem);
                recentRadioItems.remove(radioItem);
                saveChanges(ProfileItem.RECENT_RADIO);
            }
        }
    }

    public String getDownloadedMediaItemPath(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            if (MediaItem.hasMediaItemWithName(downloadedPodcasts, mediaItem)) {
                Podcast podcast = (Podcast) MediaItem.getMediaItemByName(downloadedPodcasts, mediaItem);
                for (Episod episod : podcast.getEpisods()) {
                    return episod.getAudeoUrl().replaceFirst("/.[^/]+mp3$", "");
                }
            }
        }
        return "";
    }

    public String getDownloadedTrackPath(IPlayableMediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episod) {
            if (MediaItem.hasMediaItemWithName(downloadedPodcasts, mediaItem)) {
                Podcast podcast = (Podcast) MediaItem.getMediaItemByName(downloadedPodcasts, mediaItem);
                for (Episod episod : podcast.getEpisods()) {
                    if (episod.getTitle().equals(track.getTitle())) {
                        return episod.getAudeoUrl();
                    }
                }
            }
        }
        return "";
    }

    public boolean isDownloaded(IPlayableMediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episod) {
            if (Podcast.hasMediaItemWithName(downloadedPodcasts, mediaItem)) {
                Podcast podcast = (Podcast) Podcast.getMediaItemByName(downloadedPodcasts, mediaItem);
                return Episod.hasEpisodWithTitle(podcast.getEpisods(), (Episod) track);
            }
        }
        return false;
    }

    public boolean isDownloaded(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            return MediaItem.hasMediaItemWithName(downloadedPodcasts, mediaItem);
        }
        return false;
    }

    public boolean isSubscribedToMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            return MediaItem.hasMediaItemWithName(subscribedPodcasts, mediaItem);
        } else if (mediaItem instanceof RadioItem) {
            return MediaItem.hasMediaItemWithName(subscribedRadioItems, mediaItem);
        }
        return false;
    }

    public boolean isRecentMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof RadioItem) {
            return MediaItem.hasMediaItemWithName(recentRadioItems, mediaItem);
        }
        return false;
    }

    public void removeSubscribedMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            if (MediaItem.hasMediaItemWithName(subscribedPodcasts, (Podcast) mediaItem)) {
                Podcast podcast = (Podcast) Podcast.getMediaItemByName(subscribedPodcasts, mediaItem);
                subscribedPodcasts.remove(podcast);
                saveChanges(ProfileItem.SUBSCRIBDED_PODCASTS);
            }
        } else if (mediaItem instanceof RadioItem) {
            if (RadioItem.hasMediaItemWithName(subscribedRadioItems, mediaItem)) {
                RadioItem radioItem = (RadioItem) MediaItem.getMediaItemByName(subscribedRadioItems, mediaItem);
                subscribedRadioItems.remove(radioItem);
                saveChanges(ProfileItem.SUBSCRIBDED_RADIO);
            }
        }
    }

    public void saveChanges(ProfileItem profileItem) {
        saveChanges(profileItem, true);
    }

    public void saveChanges(ProfileItem profileItem, boolean needSync) {
        String profileJsonStr = Prefs.getString(PROFILE_PREF, null);
        if (profileJsonStr != null) {
            try {
                JSONObject rootProfile = new JSONObject(profileJsonStr);
                if (profileItem == ProfileItem.DOWNLOADED_PODCASTS) {
                    rootProfile.put(JS_DOWNLOADED_PODCASTS, Podcast.toJsonArray(downloadedPodcasts, true));
                } else if (profileItem == ProfileItem.SUBSCRIBDED_PODCASTS) {
                    rootProfile.put(JS_SUBSCRIBED_PODCASTS, Podcast.toJsonArray(subscribedPodcasts, false));
                } else if (profileItem == ProfileItem.SUBSCRIBDED_RADIO) {
                    rootProfile.put(JS_SUBSCRIBED_STATIONS, RadioItem.toJsonArray(subscribedRadioItems));
                } else if (profileItem == ProfileItem.RECENT_RADIO) {
                    rootProfile.put(JS_RECENT_STATIONS, RadioItem.toJsonArray(recentRadioItems));
                }
                Prefs.putString(PROFILE_PREF, rootProfile.toString());
                Logger.printInfo(PROFILE_PREF, rootProfile.toString());
            } catch (JSONException e) {
                Logger.printInfo(PROFILE, "Can't parse profile string to json: " + profileJsonStr);
                e.printStackTrace();
            }
            if (profileSavedCallback != null) {
                new Handler(UpodsApplication.getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        profileSavedCallback.operationFinished();
                    }
                });
            }
        }
        if (needSync && LoginMaster.getInstance().isLogedIn()) {
            SyncMaster.saveToCloud();
        }
    }

    public void readFromJson(JSONObject rootProfile) {
        try {
            initProfilePodcastItem(rootProfile.getJSONArray(JS_DOWNLOADED_PODCASTS), ProfileItem.DOWNLOADED_PODCASTS);
            initProfilePodcastItem(rootProfile.getJSONArray(JS_SUBSCRIBED_PODCASTS), ProfileItem.SUBSCRIBDED_PODCASTS);
            initProfileRadioItems(rootProfile.getJSONArray(JS_SUBSCRIBED_STATIONS), ProfileItem.SUBSCRIBDED_RADIO);
            initProfileRadioItems(rootProfile.getJSONArray(JS_RECENT_STATIONS), ProfileItem.RECENT_RADIO);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getAsJson() {
        JSONObject rootProfile = null;
        try {
            String profileJsonStr = Prefs.getString(PROFILE_PREF, null);
            if (profileJsonStr == null) {
                rootProfile = new JSONObject();
            } else {
                rootProfile = new JSONObject(profileJsonStr);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return rootProfile;
    }

    public void saveToDisk(JSONObject profile) {
        if (profile != null) {
            Prefs.putString(ProfileManager.PROFILE_PREF, profile.toString());
        }
    }
}
