package com.chickenkiller.upods2.models;

import android.content.Context;

import com.chickenkiller.upods2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/4/15.
 */
public class SlidingMenuItem {

    private String title;
    private int iconId;

    public SlidingMenuItem(String title, int iconId) {
        this.title = title;
        this.iconId = iconId;
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
        allItems.add(new SlidingMenuItem("test1", R.drawable.ic_temp));
        allItems.add(new SlidingMenuItem("test1", R.drawable.ic_temp));
        allItems.add(new SlidingMenuItem("test1", R.drawable.ic_temp));
        allItems.add(new SlidingMenuItem("test1", R.drawable.ic_temp));
        return allItems;
    }

}
