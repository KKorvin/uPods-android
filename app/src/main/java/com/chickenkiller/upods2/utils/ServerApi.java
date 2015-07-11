package com.chickenkiller.upods2.utils;

/**
 * Created by alonzilberman on 7/11/15.
 */
public class ServerApi {

    public static final String DOMAIN;
    public static final String TOPS;

    static {
        DOMAIN = "https://upods.io/";
        TOPS = DOMAIN + "/upods/api/v1.0/radio-stations/top?type=";
    }
}
