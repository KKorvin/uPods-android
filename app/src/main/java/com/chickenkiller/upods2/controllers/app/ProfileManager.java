package com.chickenkiller.upods2.controllers.app;

import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;
import com.amazonaws.regions.Regions;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.models.Episod;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 9/28/15.
 */
public class ProfileManager {

    public static final String JS_DOWNLOADED_PODCASTS = "downloadedPodcasts";
    public static final String JS_SUBSCRIBED_PODCASTS = "subscribedPodcasts";
    public static final String JS_SUBSCRIBED_STATIONS = "subscribedStations";
    public static final String JS_RECENT_STATIONS = "recentStations";

    private static final String PROFILE = "PROFILE";
    private static final String AWS_DATASET_NAME = "user_profile";
    private static final int RECTNT_RADIO_STATIONS_LIMIT = 10;

    public enum ProfileItem {DOWNLOADED_PODCASTS, SUBSCRIBDED_PODCASTS, SUBSCRIBDED_RADIO, RECENT_RADIO, SUBSCRIBED_STATIONS}

    public static ProfileManager profileManager;
    private IOperationFinishCallback profileSavedCallback;

    private ArrayList<Podcast> downloadedPodcasts;
    private ArrayList<Podcast> subscribedPodcasts;
    private ArrayList<RadioItem> subscribedRadioItems;
    private ArrayList<RadioItem> recentRadioItems;

    private ProfileManager() {
        syncFromLocalStorage();
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
                    downloadedPodcasts.add(podcast);
                } else if (profileItem == ProfileItem.SUBSCRIBDED_PODCASTS) {
                    subscribedPodcasts.add(podcast);
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
                    subscribedRadioItems.add(radioItem);
                } else if (profileItem == ProfileItem.RECENT_RADIO) {
                    recentRadioItems.add(radioItem);
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
                saveChanges(ProfileItem.RECENT_RADIO);
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


    private void saveChanges(ProfileItem profileItem) {
        CognitoSyncManager syncClient = new CognitoSyncManager(
                UpodsApplication.getContext(),
                Regions.US_EAST_1,
                LoginMaster.getInstance().getCredentialsProvider());
        Dataset dataset = syncClient.openOrCreateDataset(AWS_DATASET_NAME);
        try {
            if (profileItem == ProfileItem.DOWNLOADED_PODCASTS) {
                dataset.put(JS_DOWNLOADED_PODCASTS, Podcast.toJsonArray(downloadedPodcasts, true).toString());
            } else if (profileItem == ProfileItem.SUBSCRIBDED_PODCASTS) {
                dataset.put(JS_SUBSCRIBED_PODCASTS, Podcast.toJsonArray(subscribedPodcasts, false).toString());
            } else if (profileItem == ProfileItem.SUBSCRIBDED_RADIO) {
                dataset.put(JS_SUBSCRIBED_STATIONS, RadioItem.toJsonArray(subscribedRadioItems).toString());
            } else if (profileItem == ProfileItem.RECENT_RADIO) {
                dataset.put(JS_RECENT_STATIONS, RadioItem.toJsonArray(recentRadioItems).toString());
            }
            dataset.synchronize(new DefaultSyncCallback());
            Logger.printInfo(PROFILE, "Syncying profile with AWS...");
        } catch (Exception e) {
            Logger.printInfo(PROFILE, "Can't sync profile with AWS);");
            e.printStackTrace();
        }
        if (profileSavedCallback != null) {
            profileSavedCallback.operationFinished();
        }

    }


    private void readFromDataset(Dataset dataset) {
        this.downloadedPodcasts = new ArrayList<>();
        this.subscribedPodcasts = new ArrayList<>();
        this.subscribedRadioItems = new ArrayList<>();
        this.recentRadioItems = new ArrayList<>();
        try {
            if (dataset.get(JS_DOWNLOADED_PODCASTS) != null) {
                initProfilePodcastItem(new JSONArray(dataset.get(JS_DOWNLOADED_PODCASTS)), ProfileItem.DOWNLOADED_PODCASTS);
            }
            if (dataset.get(JS_SUBSCRIBED_PODCASTS) != null) {
                initProfilePodcastItem(new JSONArray(dataset.get(JS_SUBSCRIBED_PODCASTS)), ProfileItem.SUBSCRIBDED_PODCASTS);
            }
            if (dataset.get(JS_SUBSCRIBED_STATIONS) != null) {
                initProfileRadioItems(new JSONArray(dataset.get(JS_SUBSCRIBED_STATIONS)), ProfileItem.SUBSCRIBDED_RADIO);
            }
            if (dataset.get(JS_RECENT_STATIONS) != null) {
                initProfileRadioItems(new JSONArray(dataset.get(JS_RECENT_STATIONS)), ProfileItem.RECENT_RADIO);
            }
        } catch (Exception e) {
            Logger.printError(PROFILE, "Can't get profile from cognito");
            e.printStackTrace();
        }
    }


    /**
     * Loads profile from local storage
     */
    private void syncFromLocalStorage() {
        CognitoSyncManager syncClient = new CognitoSyncManager(
                UpodsApplication.getContext(),
                Regions.US_EAST_1,
                LoginMaster.getInstance().getCredentialsProvider());
        Dataset dataset = syncClient.openOrCreateDataset(AWS_DATASET_NAME);
        readFromDataset(dataset);
    }

    /**
     * Syncs provider profile with cloud and loads last copy.
     */
    public void syncAllChanges(final IOperationFinishCallback profileSyncedCallback) {
        CognitoSyncManager syncClient = new CognitoSyncManager(
                UpodsApplication.getContext(),
                Regions.US_EAST_1,
                LoginMaster.getInstance().getCredentialsProvider());
        Dataset dataset = syncClient.openOrCreateDataset(AWS_DATASET_NAME);
        dataset.synchronize(new Dataset.SyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List<Record> updatedRecords) {
                readFromDataset(dataset);
                profileSyncedCallback.operationFinished();
            }

            @Override
            public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {
                profileSyncedCallback.operationFinished();
                return false;
            }

            @Override
            public boolean onDatasetDeleted(Dataset dataset, String datasetName) {
                profileSyncedCallback.operationFinished();
                return false;
            }

            @Override
            public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames) {
                profileSyncedCallback.operationFinished();
                return false;
            }

            @Override
            public void onFailure(DataStorageException dse) {
                Logger.printError(PROFILE, "Sync error: " + dse.getMessage());
                profileSyncedCallback.operationFinished();

            }
        });
    }

}
