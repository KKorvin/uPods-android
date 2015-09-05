package com.chickenkiller.upods2.interfaces;

import com.chickenkiller.upods2.models.Track;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 8/31/15.
 * Implement it if item has nested tracks/episods
 */
public interface ITrackable {

    void setTracks(ArrayList<? extends Track> tracks);

    ArrayList<? extends Track>  getTracks();

    String getTracksFeed();

    void selectTrack(Track track);
}
