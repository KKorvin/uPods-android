package com.chickenkiller.upods2.models;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/5/15.
 */
public class BanerItem extends MediaItem {

    public BanerItem(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    protected String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static ArrayList<BanerItem> generateDebugList(int count) {
        ArrayList<BanerItem> debugList = new ArrayList<BanerItem>();
        for (int i = 0; i < count; i++) {
            debugList.add(new BanerItem("http://www.linuxspace.org/wp-content/uploads/2015/examples/example_1.png"));
            debugList.add(new BanerItem("http://www.linuxspace.org/wp-content/uploads/2015/examples/example_2.png"));
            debugList.add(new BanerItem("http://www.linuxspace.org/wp-content/uploads/2015/examples/example_3.png"));
            debugList.add(new BanerItem("http://www.linuxspace.org/wp-content/uploads/2015/examples/example_4.png"));
            debugList.add(new BanerItem("http://www.linuxspace.org/wp-content/uploads/2015/examples/example_5.png"));
        }
        return debugList;
    }
}
