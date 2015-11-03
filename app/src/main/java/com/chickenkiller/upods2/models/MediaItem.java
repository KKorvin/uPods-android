package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/3/15.
 * Basic class for any media item, can be show in feature screen.
 */
public abstract class MediaItem implements Serializable {

    /**
     * Created by alonzilberman on 10/23/15.
     * Use it to transfer MediaItems and tracks in one object
     */
    public static class MediaItemBucket {
        public MediaItem mediaItem;
        public Track track;
    }

    protected int id;

    public MediaItem() {
    }

    public int getId() {
        return id;
    }


    public static boolean hasMediaItemWithName(ArrayList<? extends IPlayableMediaItem> mediaItems, IPlayableMediaItem mediaItemToCheck) {
        for (IPlayableMediaItem iPlayableMediaItem : mediaItems) {
            if (iPlayableMediaItem.getName().equals(mediaItemToCheck.getName())) {
                return true;
            }
        }
        return false;
    }

    public static IPlayableMediaItem getMediaItemByName(ArrayList<? extends IPlayableMediaItem> mediaItems, IPlayableMediaItem mediaItemTarget) {
        for (IPlayableMediaItem iPlayableMediaItem : mediaItems) {
            if (iPlayableMediaItem.getName().equals(mediaItemTarget.getName())) {
                return iPlayableMediaItem;
            }
        }
        return null;
    }


}
