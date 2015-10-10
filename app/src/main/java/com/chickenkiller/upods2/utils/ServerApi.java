package com.chickenkiller.upods2.utils;

/**
 * Created by alonzilberman on 7/11/15.
 */
public class ServerApi {

    public static final String DOMAIN;
    public static final String RADIO_TOP;
    public static final String RADIO_SEARCH;
    public static final String PODCASTS_TOP;
    public static final String PODCAST_CATEGORIES;
    public static final String PODCAST_SEARCH;
    public static final String PODCAST_SEARCH_PARAM;
    public static final String PODCASTS_BY_CATEGORY;

    static {
        DOMAIN = "https://upods.io";
        RADIO_TOP = DOMAIN + "/upods/api/v1.0/radio-stations/top?type=";
        RADIO_SEARCH = DOMAIN + "/upods/api/v1.0/radio-stations/search?query=";
        PODCASTS_TOP = DOMAIN + "/upods/api/v1.0/podcasts/top?type=";
        PODCAST_CATEGORIES = DOMAIN + "/upods/api/v1.0/podcasts-categories?type=all";
        PODCAST_SEARCH = "https://itunes.apple.com/search?term=";
        PODCAST_SEARCH_PARAM = "&entity=podcast";
        PODCASTS_BY_CATEGORY = DOMAIN + "/upods/api/v1.0/podcasts/category?id=";
    }
}
