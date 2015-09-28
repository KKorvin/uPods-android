package com.chickenkiller.upods2.controllers;

import android.util.Log;

import com.chickenkiller.upods2.models.Podcast;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 9/28/15.
 */
public class ProfileManager {

    private static String PROFILE_PREF = "profile_pref";
    private static String PROFILE_LOG_ERROR = "PROFILE ERROR";
    public static ProfileManager profileManager;
    private ArrayList<Podcast> downloadedPodcasts;

    private ProfileManager() {
        this.downloadedPodcasts = new ArrayList<>();
        String profileJsonStr = Prefs.getString(PROFILE_PREF, null);
        if (profileJsonStr == null) {
            JSONObject rootProfile = new JSONObject();
            JSONArray downloadedPodcasts = new JSONArray();
            JSONArray favoritePodcasts = new JSONArray();
            JSONArray followedRadioStations = new JSONArray();
            try {
                rootProfile.put("downloadedPodcasts", downloadedPodcasts);
                rootProfile.put("favoritePodcasts", favoritePodcasts);
                rootProfile.put("followedRadioStations", followedRadioStations);
            } catch (JSONException e) {
                Log.e(PROFILE_LOG_ERROR, "Can't create empty profile object");
                e.printStackTrace();
            }
            profileJsonStr = rootProfile.toString();
            Prefs.putString(PROFILE_PREF, profileJsonStr);
        }
        try {
            JSONObject rootProfile = new JSONObject(profileJsonStr);
        } catch (JSONException e) {
            Log.e(PROFILE_LOG_ERROR, "Can't parse profile string to json: " + profileJsonStr);
            e.printStackTrace();
        }
    }

    public ProfileManager getInstance() {
        if (profileManager == null) {
            profileManager = new ProfileManager();
        }
        return profileManager;
    }

    private void initDowloadedPodcasts(JSONArray downloadedPodcasts) {
        for (int i = 0; i < downloadedPodcasts.length(); i++) {
            try {
                Podcast podcast = new Podcast(downloadedPodcasts.getJSONObject(i));
            } catch (JSONException e) {
                Log.e(PROFILE_LOG_ERROR, "Can't create podcast from json object at index" + String.valueOf(i));
                e.printStackTrace();
            }

        }
    }

}
