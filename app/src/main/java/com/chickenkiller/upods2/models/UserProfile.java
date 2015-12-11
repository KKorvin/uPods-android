package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;

/**
 * Created by alonzilberman on 12/11/15.
 */
public class UserProfile {

    public static String EMPTY_AVATAR = "https://cdn2.iconfinder.com/data/icons/social-flat-buttons-3/512/anonymous-512.png";

    private String name;
    private String email;
    private String profileImageUrl;

    public UserProfile() {
        this.name = UpodsApplication.getContext().getString(R.string.anonymus);
        this.email = "";
        this.profileImageUrl = EMPTY_AVATAR;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
