package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.interfaces.IMediaItemView;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/3/15.
 * Basic class for any media item, can be show in feature screen.
 */
public abstract class MediaItem implements IMediaItemView {

    /**
     * Created by alonzilberman on 10/23/15.
     * Use it to transfer MediaItems and tracks in one object
     */
    public static class MediaItemBucket {
        public MediaItem mediaItem;
        public Track track;
    }

    protected int id;
    protected String name;
    protected String coverImageUrl;


    public MediaItem() {
    }

    public int getId() {
        return id;
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

    public static boolean hasMediaItemWithName(ArrayList<? extends MediaItem> mediaItems, MediaItem mediaItemToCheck) {
        for (MediaItem mediaItem : mediaItems) {
            if (mediaItem.getName().equals(mediaItemToCheck.getName())) {
                return true;
            }
        }
        return false;
    }

    public static MediaItem getMediaItemByName(ArrayList<? extends MediaItem> mediaItems, MediaItem mediaItemTarget) {
        for (MediaItem mediaItem : mediaItems) {
            if (mediaItem.getName().equals(mediaItemTarget.getName())) {
                return mediaItem;
            }
        }
        return null;
    }


}
