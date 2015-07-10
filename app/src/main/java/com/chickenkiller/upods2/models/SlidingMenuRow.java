package com.chickenkiller.upods2.models;

import android.content.Context;

import com.chickenkiller.upods2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/4/15.
 */
public class SlidingMenuRow extends SlidingMenuItem {

    private String title;
    private int iconId;

    public SlidingMenuRow(String title, int iconId) {
        super();
        this.title = title;
        this.iconId = iconId;
    }

    public SlidingMenuRow(String title, int iconId, boolean hasDevider) {
        this(title, iconId);
        this.hasDevider = hasDevider;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public static List<SlidingMenuItem> fromDefaultSlidingMenuSet(Context mContext) {
        ArrayList<SlidingMenuItem> allItems = new ArrayList<SlidingMenuItem>();
        allItems.add(new SlidingMenuRow(mContext.getString(R.string.profile_my_profile), R.drawable.ic_account_circle_black_36dp));
        allItems.add(new SlidingMenuRow(mContext.getString(R.string.radio_main), R.drawable.ic_radio_black_36dp));
        allItems.add(new SlidingMenuRow(mContext.getString(R.string.podcasts_main), R.drawable.ic_speaker_black_36dp));
        allItems.add(new SlidingMenuRow(mContext.getString(R.string.cloud_sync), R.drawable.ic_cloud_upload_black_36dp, true));
        allItems.add(new SlidingMenuRow(mContext.getString(R.string.main_rate_app), R.drawable.ic_favorite_black_36dp));
        allItems.add(new SlidingMenuRow(mContext.getString(R.string.main_about), R.drawable.ic_android_black_36dp));
        allItems.add(new SlidingMenuRow(mContext.getString(R.string.main_help), R.drawable.ic_help_black_36dp, true));
        allItems.add(new SlidingMenuRow(mContext.getString(R.string.main_settings), R.drawable.ic_settings_black_36dp));
        return allItems;
    }

}
