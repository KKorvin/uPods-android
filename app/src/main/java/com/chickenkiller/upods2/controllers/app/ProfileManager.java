package com.chickenkiller.upods2.controllers.app;

import android.util.Log;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.models.Episod;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.Track;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 9/28/15.
 */
public class ProfileManager {

    public enum ProfileItem {DOWNLOADED_PODCASTS, SUBSCRIBDED_PODCASTS, SUBSCRIBED_STATIONS}

    private static String PROFILE_PREF = "profile_pref";
    private static String PROFILE = "PROFILE";
    public static ProfileManager profileManager;
    private ArrayList<Podcast> downloadedPodcasts;
    private ArrayList<Podcast> subscribedPodcasts;
    private IOperationFinishCallback profileSavedCallback;

    private ProfileManager() {
        this.downloadedPodcasts = new ArrayList<>();
        this.subscribedPodcasts = new ArrayList<>();
        String profileJsonStr = Prefs.getString(PROFILE_PREF, null);
        if (profileJsonStr == null) {
            JSONObject rootProfile = new JSONObject();
            JSONArray downloadedPodcasts = new JSONArray();
            JSONArray subscribedPodcasts = new JSONArray();
            JSONArray subscribedRadioStations = new JSONArray();
            try {
                rootProfile.put("downloadedPodcasts", downloadedPodcasts);
                rootProfile.put("subscribedPodcasts", subscribedPodcasts);
                rootProfile.put("subscribedStations", subscribedRadioStations);
            } catch (JSONException e) {
                Log.e(PROFILE, "Can't create empty profile object");
                e.printStackTrace();
            }
            profileJsonStr = rootProfile.toString();
            Prefs.putString(PROFILE_PREF, profileJsonStr);
        }
        try {
            JSONObject rootProfile = new JSONObject(profileJsonStr);
            initProfilePodcastItem(rootProfile.getJSONArray("downloadedPodcasts"), ProfileItem.DOWNLOADED_PODCASTS);
            initProfilePodcastItem(rootProfile.getJSONArray("subscribedPodcasts"), ProfileItem.SUBSCRIBDED_PODCASTS);
        } catch (JSONException e) {
            Log.e(PROFILE, "Can't parse profile string to json: " + profileJsonStr);
            e.printStackTrace();
        }
    }

    public static ProfileManager getInstance() {
        if (profileManager == null) {
            profileManager = new ProfileManager();
        }
        return profileManager;
    }

    public void setOperationFinishCallback(IOperationFinishCallback profileSavedCallback) {
        this.profileSavedCallback = profileSavedCallback;
    }

    public ArrayList<Podcast> getDownloadedPodcasts() {
        return this.downloadedPodcasts;
    }

    public ArrayList<Podcast> getSubscribedPodcasts() {
        return this.subscribedPodcasts;
    }

    private void initProfilePodcastItem(JSONArray podcasts, ProfileItem profileItem) {
        for (int i = 0; i < podcasts.length(); i++) {
            try {
                Podcast podcast = new Podcast(podcasts.getJSONObject(i));
                if (profileItem == ProfileItem.DOWNLOADED_PODCASTS) {
                    downloadedPodcasts.add(podcast);
                } else if (profileItem == ProfileItem.SUBSCRIBDED_PODCASTS) {
                    subscribedPodcasts.add(podcast);
                }
            } catch (JSONException e) {
                Log.e(PROFILE, "Can't fetch podcast from json object at index" + String.valueOf(i));
                e.printStackTrace();
            }
        }
        String podcastType = "";
        if (profileItem == ProfileItem.DOWNLOADED_PODCASTS) {
            podcastType = " downloaded ";
        } else if (profileItem == ProfileItem.SUBSCRIBDED_PODCASTS) {
            podcastType = " subscribded ";
        }
        Log.i(PROFILE, "Fetcheed " + String.valueOf(podcasts.length()) + podcastType + "podcasts from json profile");
    }

    public void addSubscribedMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            if (!Podcast.hasPodcastWithName(subscribedPodcasts, (Podcast) mediaItem)) {
                subscribedPodcasts.add((Podcast) mediaItem);
                saveChanges(ProfileItem.SUBSCRIBDED_PODCASTS);
            }
        }
    }

    public void addDownloadedTrack(IPlayableMediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episod) {
            if (!Podcast.hasPodcastWithName(downloadedPodcasts, (Podcast) mediaItem)) {
                Podcast podcast = new Podcast((Podcast) mediaItem);
                podcast.getEpisods().clear();
                podcast.getEpisods().add((Episod) track);
                downloadedPodcasts.add(podcast);
            } else {
                Podcast podcast = Podcast.getPodcastByName(downloadedPodcasts, (Podcast) mediaItem);
                podcast.getEpisods().add((Episod) track);
            }
            saveChanges(ProfileItem.DOWNLOADED_PODCASTS);
        }
    }

    public void removeDownloadedTrack(IPlayableMediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episod) {
            if (Podcast.hasPodcastWithName(downloadedPodcasts, (Podcast) mediaItem)) {
                Podcast podcast = Podcast.getPodcastByName(downloadedPodcasts, (Podcast) mediaItem);
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
            if (Podcast.hasPodcastWithName(downloadedPodcasts, (Podcast) mediaItem)) {
                Podcast podcast = Podcast.getPodcastByName(downloadedPodcasts, (Podcast) mediaItem);
                downloadedPodcasts.remove(podcast);
                saveChanges(ProfileItem.DOWNLOADED_PODCASTS);
            }
        }
    }

    public String getDownloadedMediaItemPath(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            if (Podcast.hasPodcastWithName(downloadedPodcasts, (Podcast) mediaItem)) {
                Podcast podcast = Podcast.getPodcastByName(downloadedPodcasts, (Podcast) mediaItem);
                for (Episod episod : podcast.getEpisods()) {
                    return episod.getAudeoUrl().replaceFirst("/.[^/]+mp3$","");
                }
            }
        }
        return "";
    }

    public boolean isDownloaded(IPlayableMediaItem mediaItem, Track track) {
        if (mediaItem instanceof Podcast && track instanceof Episod) {
            if (Podcast.hasPodcastWithName(downloadedPodcasts, (Podcast) mediaItem)) {
                Podcast podcast = Podcast.getPodcastByName(downloadedPodcasts, (Podcast) mediaItem);
                return Episod.hasEpisodWithTitle(podcast.getEpisods(), (Episod) track);
            }
        }
        return false;
    }

    public boolean isDownloaded(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            return Podcast.hasPodcastWithName(downloadedPodcasts, (Podcast) mediaItem);
        }
        return false;
    }

    public boolean isSubscribedToMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            return Podcast.hasPodcastWithName(subscribedPodcasts, (Podcast) mediaItem);
        }
        return false;
    }

    public void removeSubscribedMediaItem(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            if (Podcast.hasPodcastWithName(subscribedPodcasts, (Podcast) mediaItem)) {
                Podcast podcast = Podcast.getPodcastByName(subscribedPodcasts, (Podcast) mediaItem);
                subscribedPodcasts.remove(podcast);
                saveChanges(ProfileItem.SUBSCRIBDED_PODCASTS);
            }
        }
    }

    /**
     * @param mediaItem
     * @return id of string which is status of medida item (i.e downloaded, subscribed etc)
     */
    public int getItemStatus(IPlayableMediaItem mediaItem) {
        if (mediaItem instanceof Podcast) {
            if (Podcast.hasPodcastWithName(downloadedPodcasts, (Podcast) mediaItem)) {
                return R.string.downloaded;
            } else if (Podcast.hasPodcastWithName(downloadedPodcasts, (Podcast) mediaItem)) {
                return R.string.subscribed;
            }
        }
        return R.string.none;
    }

    private void saveChanges(ProfileItem profileItem) {
        String profileJsonStr = Prefs.getString(PROFILE_PREF, null);
        if (profileJsonStr != null) {
            try {
                JSONObject rootProfile = new JSONObject(profileJsonStr);
                if (profileItem == ProfileItem.DOWNLOADED_PODCASTS) {
                    rootProfile.put("downloadedPodcasts", Podcast.toJsonArray(downloadedPodcasts, true));
                } else if (profileItem == ProfileItem.SUBSCRIBDED_PODCASTS) {
                    rootProfile.put("subscribedPodcasts", Podcast.toJsonArray(subscribedPodcasts, false));
                }
                Prefs.putString(PROFILE_PREF, rootProfile.toString());
                Log.i(PROFILE_PREF, rootProfile.toString());
            } catch (JSONException e) {
                Log.e(PROFILE, "Can't parse profile string to json: " + profileJsonStr);
                e.printStackTrace();
            }
            if (profileSavedCallback != null) {
                profileSavedCallback.operationFinished();
            }
        }
    }
}
