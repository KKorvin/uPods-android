package com.chickenkiller.upods2.models;

/**
 * Created by alonzilberman on 7/7/15.
 */
public class MediaItemTitle extends MediaItem {

    private String title;

    public String getTitle() {
        return title;
    }

    public MediaItemTitle(String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
