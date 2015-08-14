package com.chickenkiller.upods2.utils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by alonzilberman on 8/14/15.
 */
public class GlobalUtils {

    private static final String[] bestStreamPatterns = {"\\.mp3", "[^.]{4}$"};


    public static String getBestStreamUrl(JSONArray allUrls) {
        String url = "";
        try {
            url = allUrls.getString(0);
            for (String pattern : bestStreamPatterns) {
                for (int i = 0; i < allUrls.length(); i++) {
                    if (allUrls.getString(i).matches(pattern)) {
                        url = allUrls.getString(i);
                        return url;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return url;
    }

}
