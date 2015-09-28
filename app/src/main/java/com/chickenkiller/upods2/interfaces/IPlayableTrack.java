package com.chickenkiller.upods2.interfaces;

/**
 * Created by alonzilberman on 8/31/15.
 * Implement this interface to make model shown in middle screen and be playable as part of media item
 */
public interface IPlayableTrack {

    String getTitle();

    String getSubTitle();

    String getAudeoUrl();

    String getDate();

}
