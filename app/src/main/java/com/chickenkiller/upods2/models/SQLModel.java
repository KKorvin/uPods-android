package com.chickenkiller.upods2.models;

/**
 * Created by Alon Zilberman on 16/03/2016.
 */
abstract public class SQLModel {

    public boolean isExistsInDb;
    public long id;

    public SQLModel() {
        this.id = -1;
        this.isExistsInDb = false;
    }

    public long getId() {
        return id;
    }

}
