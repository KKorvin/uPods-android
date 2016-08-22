package com.chickenkiller.upods2.utils;

import android.util.Log;

/**
 * Created by Alon Zilberman on 11/3/15.
 */
public class Logger {

    private static boolean isTurnedOn = false;

    public static void printInfo(String tag, String text) {
        if (isTurnedOn) {
            Log.i(tag, text);
        }
    }

    public static void printInfo(String tag, int text) {
        if (isTurnedOn) {
            Log.i(tag, String.valueOf(text));
        }
    }

    public static void printInfo(String tag, boolean text) {
        if (isTurnedOn) {
            Log.i(tag, String.valueOf(text));
        }
    }


    public static void printError(String tag, String text) {
        if (isTurnedOn) {
            Log.i(tag, text);
        }
    }
}
