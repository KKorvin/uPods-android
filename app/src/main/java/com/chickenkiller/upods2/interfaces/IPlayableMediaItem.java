package com.chickenkiller.upods2.interfaces;

import java.io.Serializable;

/**
 * Created by alonzilberman on 8/30/15.
 * Implement this interface by making item playable by player and shown by middle screen
 */
public interface IPlayableMediaItem extends Serializable {

    String getCoverImageUrl();

    String getSubHeader();

    String getBottomHeader();

    String getName();

    String getDescription();

    String getAudeoLink();

    boolean hasTracks();
}
