package com.chickenkiller.upods2.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chickenkiller.upods2.controllers.app.UpodsApplication;

import java.util.ArrayList;

/**
 * Created by Alon Zilberman on 17/03/2016.
 */
public class MediaListItem extends SQLModel {

    public static final String SUBSCRIBED = "subscribed";
    public static final String RECENT = "recent";
    public static final String TYPE_PODCAST = "podcast";
    public static final String TYPE_RADIO = "radio";
    public static final String DOWNLOADED = "downloaded";
    public static final String NEW = "new";

    public String mediaItemName;
    public String listType;

    public MediaListItem() {
        super();
    }

    public static ArrayList<MediaListItem> withMediaType(String mediaType) {
        ArrayList<MediaListItem> mediaListItems = new ArrayList<>();
        SQLiteDatabase database = UpodsApplication.getDatabaseManager().getReadableDatabase();
        String[] args = {mediaType};
        String table = "radio_stations";
        if (mediaType.equals(TYPE_PODCAST)) {
            table = "podcasts";
        }
        Cursor cursor = database.rawQuery("SELECT r.id, r.name, m.list_type FROM " + table + " as r\n" +
                "LEFT JOIN media_list as m ON r.id = m.media_id\n" +
                "WHERE m.media_type =  ?", args);
        while (cursor.moveToNext()) {
            MediaListItem mediaListItem = new MediaListItem();
            mediaListItem.isExistsInDb = true;
            mediaListItem.id = cursor.getLong(cursor.getColumnIndex("id"));
            mediaListItem.mediaItemName = cursor.getString(cursor.getColumnIndex("name"));
            mediaListItem.listType = cursor.getString(cursor.getColumnIndex("list_type"));
            mediaListItems.add(mediaListItem);
        }
        cursor.close();
        return mediaListItems;
    }
}
