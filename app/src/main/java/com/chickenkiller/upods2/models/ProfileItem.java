package com.chickenkiller.upods2.models;

import android.app.Activity;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;

/**
 * Created by Alon Zilberman on 2/26/16.
 */
public class ProfileItem {
    public String leftText;
    public String rightText;

    public ProfileItem(String leftText, String rightText) {
        this.leftText = leftText;
        this.rightText = rightText;
    }

    public ProfileItem(String leftText, int rightText) {
        this(leftText, String.valueOf(rightText));
    }

    public static ArrayList<ProfileItem> fromLoggedinUser(Activity activity) {
        ArrayList<ProfileItem> profileItems = new ArrayList<>();
        ProfileManager profileManager = ProfileManager.getInstance();
        profileItems.add(new ProfileItem(activity.getString(R.string.subscribed_radio_stations), profileManager.getSubscribedRadioItems().size()));
        profileItems.add(new ProfileItem(activity.getString(R.string.recent_raio_station), profileManager.getRecentRadioItems().size()));
        profileItems.add(new ProfileItem(activity.getString(R.string.subscribed_podcasts), profileManager.getSubscribedPodcasts().size()));
        profileItems.add(new ProfileItem(activity.getString(R.string.downloaded_podcasts), profileManager.getDownloadedPodcasts().size()));
        profileItems.add(new ProfileItem(activity.getString(R.string.last_sync_with_cloud), Prefs.getString(SettingsManager.PREFS_LAST_CLOUD_SYNC, "")));
        return profileItems;
    }

}
