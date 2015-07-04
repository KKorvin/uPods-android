package com.chickenkiller.upods2.models;

/**
 * Created by alonzilberman on 7/3/15.
 */
public abstract class MediaItem {

    protected String name;
    protected String url;
    protected String imageUrl;

    public MediaItem(){

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
