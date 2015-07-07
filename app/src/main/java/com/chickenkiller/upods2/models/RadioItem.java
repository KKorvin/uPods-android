package com.chickenkiller.upods2.models;

import android.content.Context;

import com.chickenkiller.upods2.R;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/3/15.
 */
public class RadioItem extends MediaItem {
    protected String name;
    protected String url;
    protected String imageUrl;

    public RadioItem(String name, String url, String imageUrl) {
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public static ArrayList<MediaItem> generateDebugList(int count, Context mContext) {
        ArrayList<MediaItem> debugList = new ArrayList<MediaItem>();
        debugList.add(new MediaItemTitle(mContext.getString(R.string.top40_chanels)));
        for (int i = 0; i < count; i++) {
            debugList.add(new RadioItem("Test" + String.valueOf(i),
                    "", "https://igcdn-photos-g-a.akamaihd.net/hphotos-ak-xaf1/t51.2885-15/11356537_1775007156059126_1886851436_n.jpg"));
        }
        return debugList;
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
