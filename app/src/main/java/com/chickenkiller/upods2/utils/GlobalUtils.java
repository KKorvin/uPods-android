package com.chickenkiller.upods2.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import com.chickenkiller.upods2.controllers.app.UpodsApplication;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alonzilberman on 8/14/15.
 */
public class GlobalUtils {

    private static final String[] bestStreamPatterns = {".+\\.mp3", ".+[^.]{4}$"};


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

    public static String parserDateToUS(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z");
            Date inputDate = dateFormat.parse(date);
            dateFormat = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
            date = dateFormat.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String parserDateToMonth(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z");
            Date inputDate = dateFormat.parse(date);
            dateFormat = new SimpleDateFormat("MMM\ndd");
            date = dateFormat.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getCleanFileName(String str){
        str = str.toLowerCase();
        return str.replaceAll("[^a-zA-Z0-9]+","_");
    }

    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }

    public static boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) UpodsApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
