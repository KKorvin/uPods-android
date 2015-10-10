package com.chickenkiller.upods2.models;

import android.util.Log;

import com.chickenkiller.upods2.controllers.BackendManager;
import com.chickenkiller.upods2.interfaces.INetworkUIupdater;
import com.chickenkiller.upods2.utils.ServerApi;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 10/7/15.
 */
public class Category {

    private final static String CATEGORIES_LOG = "Categories";
    private final static String CATEGORIES_PREF = "podcasts_categories";
    private String name;
    private int id;

    public Category(String name) {
        this.id = -1;
        this.name = name;
    }

    public Category(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public static List<Category> getPodcastsCategoriesList() {
        String categoriesJsonStr = Prefs.getString(CATEGORIES_PREF, null);
        ArrayList<Category> podcastsCategories = new ArrayList<>();
        try {
            JSONArray jsonCategories = jsonCategories = new JSONArray(categoriesJsonStr);
            for (int i = 0; i < jsonCategories.length(); i++) {
                podcastsCategories.add(new Category(jsonCategories.getJSONObject(i).getString("name"),
                        jsonCategories.getJSONObject(i).getInt("id")));
            }
        } catch (JSONException e) {
            Log.i(CATEGORIES_LOG, "Can't get podcasts categories from shared prefs");
            e.printStackTrace();
        }
        return podcastsCategories;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    /**
     * Call it in application launch to put podcasts categories into the memory
     */
    public static void initCatrgories() {
        String categoriesJsonStr = Prefs.getString(CATEGORIES_PREF, null);
        if (categoriesJsonStr == null) {
            BackendManager.getInstance().sendRequest(ServerApi.PODCAST_CATEGORIES, new INetworkUIupdater() {
                @Override
                public void updateUISuccess(JSONObject jResponse) {
                    try {
                        JSONArray result = jResponse.getJSONArray("result");
                        Prefs.putString(CATEGORIES_PREF, result.toString());
                        Log.i(CATEGORIES_LOG, "Got " + String.valueOf(result.length()) + " podcasts categories from server");
                    } catch (JSONException e) {
                        Log.i(CATEGORIES_LOG, "Can't get podcasts categories from server");
                        e.printStackTrace();
                    }
                }

                @Override
                public void updateUIFailed() {

                }
            });
        }
    }

    public void setName(String name) {
        this.name = name;
    }
}
