package com.chickenkiller.upods2.models;

/**
 * Created by alonzilberman on 8/31/15.
 */
public abstract class Track {

    protected String title;
    protected String mp3Url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMp3Url() {
        return mp3Url;
    }

    public void setMp3Url(String mp3Url) {
        this.mp3Url = mp3Url;
    }
}
