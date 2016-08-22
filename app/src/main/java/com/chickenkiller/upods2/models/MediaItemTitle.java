package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.interfaces.IMediaItemView;

/**
 * Created by Alon Zilberman on 7/7/15.
 */
public class MediaItemTitle implements IMediaItemView {

    private String title;
    private String subTitle;
    public boolean showButton;

    public String getTitle() {
        return title;
    }

    public MediaItemTitle(String title) {
        super();
        this.subTitle = "";
        this.showButton = true;
        this.title = title;
    }

    public MediaItemTitle(String title, String subTitle) {
        this.title = title;
        this.subTitle = subTitle;
        this.showButton = false;
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
