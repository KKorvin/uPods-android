package com.chickenkiller.upods2.models;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/5/15.
 */
public class BanerItem extends MediaItem {

    public BanerItem(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static ArrayList<BanerItem> generateDebugList(int count) {
        ArrayList<BanerItem> debugList = new ArrayList<BanerItem>();
        for (int i = 0; i < count; i++) {
            debugList.add(new BanerItem("http://www.topofandroid.com/wp-content/uploads/2015/05/ahagzjsozh.jpg"));
            debugList.add(new BanerItem("http://www.lirent.net/wp-content/uploads/2014/10/Android-Lollipop-wallpapers-p-800x500.png"));
            debugList.add(new BanerItem("http://www.img.lirent.net/2014/10/Android-Lollipop-wallpapers-Wall1.png"));
            debugList.add(new BanerItem("https://smexyyweby.files.wordpress.com/2010/11/header-mana1.jpg"));
        }
        return debugList;
    }
}
