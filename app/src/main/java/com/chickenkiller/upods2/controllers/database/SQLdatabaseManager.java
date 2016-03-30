package com.chickenkiller.upods2.controllers.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alonzilberman on 16/03/2016.
 */
public class SQLdatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "upods.db";
    private static final int CUR_DATABASE_VERSION = 1;
    private static SQLdatabaseManager databaseManager;

    public SQLdatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE podcasts (\n" +
                "    id              INTEGER       PRIMARY KEY\n" +
                "                                  UNIQUE,\n" +
                "    name            VARCHAR (255) DEFAULT '',\n" +
                "    censored_name   VARCHAR (255) DEFAULT '',\n" +
                "    artist_name     VARCHAR (255) DEFAULT '',\n" +
                "    description     TEXT (2000)   DEFAULT '',\n" +
                "    feed_url        VARCHAR (500) DEFAULT '',\n" +
                "    cover_image_url VARCHAR (255) DEFAULT '',\n" +
                "    release_date    VARCHAR (255) DEFAULT '',\n" +
                "    explicitness    VARCHAR (255) DEFAULT '',\n" +
                "    country         VARCHAR (100) DEFAULT '',\n" +
                "    genre           VARCHAR (255) DEFAULT '',\n" +
                "    track_count     VARCHAR (100) DEFAULT ''\n" +
                ");");

        db.execSQL("CREATE TABLE radio_stations (\n" +
                "    id              INTEGER        PRIMARY KEY\n" +
                "                                   UNIQUE,\n" +
                "    name            VARCHAR (255)  DEFAULT '',\n" +
                "    description     VARCHAR (2000) DEFAULT '',\n" +
                "    website         VARCHAR (255)  DEFAULT '',\n" +
                "    facebook        VARCHAR (255)  DEFAULT '',\n" +
                "    twitter         VARCHAR (255)  DEFAULT '',\n" +
                "    cover_image_url VARCHAR (255)  DEFAULT '',\n" +
                "    country         VARCHAR (100)  DEFAULT '',\n" +
                "    genre           VARCHAR (255)  DEFAULT ''\n" +
                ");\n");

        db.execSQL("CREATE TABLE stream_link (\n" +
                "    id      INTEGER       PRIMARY KEY\n" +
                "                          UNIQUE,\n" +
                "    radio_station_id INTEGER,\n" +
                "    url     VARCHAR (300) DEFAULT '',\n" +
                "    bitrate VARCHAR (50)  DEFAULT ''\n" +
                ");\n");

        db.execSQL("CREATE TABLE episodes (\n" +
                "    id         INTEGER       PRIMARY KEY\n" +
                "                             UNIQUE,\n" +
                "    podcast_id INTEGER,\n" +
                "    title      VARCHAR (300) DEFAULT '',\n" +
                "    summary    TEXT (1000)   DEFAULT '',\n" +
                "    length     VARCHAR (100) DEFAULT '',\n" +
                "    duration   VARCHAR (100) DEFAULT '',\n" +
                "    date       VARCHAR (100) DEFAULT '',\n" +
                "    pathOnDisk VARCHAR (255) DEFAULT ''\n" +
                ");\n");

        db.execSQL("CREATE TABLE podcasts_episodes_rel (\n" +
                "    id         INTEGER      PRIMARY KEY AUTOINCREMENT\n" +
                "                            UNIQUE,\n" +
                "    podcast_id INTEGER,\n" +
                "    episode_id INTEGER,\n" +
                "    type       VARCHAR (20) \n" +
                ");\n");

        db.execSQL("CREATE TABLE media_list (\n" +
                "    id         INTEGER      PRIMARY KEY AUTOINCREMENT\n" +
                "                            UNIQUE,\n" +
                "    media_id   INTEGER,\n" +
                "    media_type VARCHAR (20),\n" +
                "    list_type  VARCHAR (20) \n" +
                ");\n");

        //Position in seconds
        db.execSQL("CREATE TABLE tracks_positions (\n" +
                "                id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                "                track_name VARCHAR (300) UNIQUE,\n" +
                "                position INTEGER);");

        db.execSQL("CREATE UNIQUE INDEX track_name_idx ON tracks_positions (track_name);");

        db.execSQL("CREATE TABLE streams_quality (\n" +
                "                id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                "                media_item_name VARCHAR (300) UNIQUE,\n" +
                "                quality VARCHAR (100));");

        db.execSQL("CREATE UNIQUE INDEX media_item_name_idx ON streams_quality (media_item_name);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }
}
