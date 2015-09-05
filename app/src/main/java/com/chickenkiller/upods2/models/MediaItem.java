package com.chickenkiller.upods2.models;

import java.io.Serializable;

/**
 * Created by alonzilberman on 7/3/15.
 * Basic class for any media item, can be show in feature screen.
 */
public abstract class MediaItem implements Serializable{

    protected int id;

    public MediaItem() {}

    public int getId() {
        return id;
    }

}
