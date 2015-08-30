package com.chickenkiller.upods2.interfaces;

import java.io.Serializable;

/**
 * Created by alonzilberman on 8/30/15.
 */
public interface IPlayableMediaItem extends Serializable {

    String getCoverImageUrl();

    String getSubHeader();

    String getName();

    /**
     * @return if item has nested tracks (i.e episods for podcasts)
     */
    boolean hasSubTracks();

    String getDescription();

    String getStreamUrl();
}
