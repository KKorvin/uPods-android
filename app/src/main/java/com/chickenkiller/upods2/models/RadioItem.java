package com.chickenkiller.upods2.models;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/3/15.
 */
public class RadioItem extends MediaItem {

    public RadioItem(String name, String url, String imageUrl) {
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public static ArrayList<MediaItem> generateDebugList(int count) {
        ArrayList<MediaItem> debugList = new ArrayList<MediaItem>();
        for (int i = 0; i < count; i++) {
            debugList.add(new RadioItem("Test" + String.valueOf(i),
                    "","https://media.licdn.com/mpr/mpr/shrinknp_400_400/p/6/005/06d/0cb/20e5317.jpg"));
        }
        return debugList;
    }
}
