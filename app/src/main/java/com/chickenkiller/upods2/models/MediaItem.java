package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.interfaces.IMediaItemView;
import com.chickenkiller.upods2.utils.GlobalUtils;

import java.util.ArrayList;

/**
 * Created by Alon Zilberman on 7/3/15.
 * Basic class for any media item, can be show in feature screen.
 */
public abstract class MediaItem extends SQLModel implements IMediaItemView {

    /**
     * Created by Alon Zilberman on 10/23/15.
     * Use it to transfer MediaItems and tracks in one object
     */
    public static class MediaItemBucket {
        public MediaItem mediaItem;
        public Track track;
    }

    protected String name;
    protected String coverImageUrl;
    protected float score;


    public boolean isSubscribed;

    public MediaItem() {
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getSubHeader() {
        return "";
    }

    public String getBottomHeader() {
        return "";
    }

    public boolean hasTracks() {
        return false;
    }

    public String getDescription() {
        return "";
    }

    public String getAudeoLink() {
        return null;
    }

    public String getBitrate() {
        return "";
    }


    public void syncWithDB(){

    }

    public void syncWithMediaItem(MediaItem updatedMediaItem) {
        this.id = updatedMediaItem.id;
        this.isSubscribed = updatedMediaItem.isSubscribed;
        this.isExistsInDb = updatedMediaItem.isExistsInDb;
    }

    public static boolean hasMediaItemWithName(ArrayList<? extends MediaItem> mediaItems, MediaItem mediaItemToCheck) {
        for (MediaItem mediaItem : mediaItems) {
            if (mediaItem.getName().equals(mediaItemToCheck.getName())) {
                return true;
            }
        }
        return false;
    }

    public static MediaItem getMediaItemByName(ArrayList<? extends MediaItem> mediaItems, MediaItem mediaItemTarget) {
        return getMediaItemByName(mediaItems, mediaItemTarget.getName());
    }

    public static MediaItem getMediaItemByName(ArrayList<? extends MediaItem> mediaItems, String targetName) {
        for (MediaItem mediaItem : mediaItems) {
            if (GlobalUtils.safeTitleEquals(mediaItem.getName(), targetName)) {
                return mediaItem;
            }
        }
        return null;
    }

    public static ArrayList<String> getIds(ArrayList<? extends MediaItem> mediaItems) {
        ArrayList<String> ids = new ArrayList<>();
        for (MediaItem mediaItem : mediaItems) {
            ids.add(String.valueOf(mediaItem.id));
        }
        return ids;
    }

    public float getScore(){
        return score;
    }
}
