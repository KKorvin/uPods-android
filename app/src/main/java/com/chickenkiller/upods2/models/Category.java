package com.chickenkiller.upods2.models;

import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.interfaces.IRequestCallback;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ServerApi;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 10/7/15.
 * Used for podcasts categories, music genres, languages and countries.
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
        return getCategoriesListFromJSON(categoriesJsonStr);
    }

    public static List<Category> getCategoriesListFromJSON(String categoriesJsonStr) {
        ArrayList<Category> categories = new ArrayList<>();
        try {
            JSONArray jsonCategories = jsonCategories = new JSONArray(categoriesJsonStr);
            for (int i = 0; i < jsonCategories.length(); i++) {
                String name = jsonCategories.getJSONObject(i).has("name") ? jsonCategories.getJSONObject(i).getString("name") : "";
                int id = jsonCategories.getJSONObject(i).has("id") ? jsonCategories.getJSONObject(i).getInt("id") : -1;
                categories.add(new Category(name, id));
            }
        } catch (Exception e) {
            Logger.printInfo(CATEGORIES_LOG, "Can't get podcasts categories from shared prefs");
            e.printStackTrace();
        }
        return categories;
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
    public static void initPodcastsCatrgories() {
        String categoriesJsonStr = Prefs.getString(CATEGORIES_PREF, null);
        if (categoriesJsonStr == null) {
            BackendManager.getInstance().sendRequest(ServerApi.PODCAST_CATEGORIES, new IRequestCallback() {
                @Override
                public void onRequestSuccessed(JSONObject jResponse) {
                    try {
                        JSONArray result = jResponse.getJSONArray("result");
                        Prefs.putString(CATEGORIES_PREF, result.toString());
                        Logger.printInfo(CATEGORIES_LOG, "Got " + String.valueOf(result.length()) + " podcasts categories from server");
                    } catch (JSONException e) {
                        Logger.printInfo(CATEGORIES_LOG, "Can't get podcasts categories from server");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRequestFailed() {

                }
            });
        }
    }

    public void setName(String name) {
        this.name = name;
    }
}
