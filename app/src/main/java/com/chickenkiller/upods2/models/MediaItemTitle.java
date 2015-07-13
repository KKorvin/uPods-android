package com.chickenkiller.upods2.models;

/**
 * Created by alonzilberman on 7/7/15.
 */
public class MediaItemTitle extends MediaItem {

    private String title;
    private String subTitle;

    public String getTitle() {
        return title;
    }

    public MediaItemTitle(String title) {
        this.title = title;
    }

    public MediaItemTitle(String title, String subTitle) {
        this.title = title;
        this.subTitle = subTitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
}
