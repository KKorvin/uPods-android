package com.chickenkiller.upods2.models;

/**
 * Created by alonzilberman on 7/3/15.
 */
public abstract class MediaItem {

    protected String imageUrl;

    public MediaItem() {

    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
