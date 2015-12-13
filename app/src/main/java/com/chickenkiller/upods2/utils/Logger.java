package com.chickenkiller.upods2.utils;

import android.util.Log;

/**
 * Created by alonzilberman on 11/3/15.
 */
public class Logger {

    public static void printInfo(String tag, String text) {
        Log.i(tag, text);
    }

    public static void printInfo(String tag, int text) {
        Log.i(tag, String.valueOf(text));
    }

    public static void printError(String tag, String text) {
        Log.i(tag, text);
    }
}
