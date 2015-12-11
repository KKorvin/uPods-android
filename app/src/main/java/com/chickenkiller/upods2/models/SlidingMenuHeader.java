package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;

/**
 * Created by alonzilberman on 7/10/15.
 */
public class SlidingMenuHeader extends SlidingMenuItem {

    private String name;
    private String email;
    private String imgUrl;

    public SlidingMenuHeader() {
        super();
        this.name = UpodsApplication.getContext().getString(R.string.anonymus);
        this.email = UpodsApplication.getContext().getString(R.string.anonymus_email);
        this.imgUrl = UserProfile.EMPTY_AVATAR;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
