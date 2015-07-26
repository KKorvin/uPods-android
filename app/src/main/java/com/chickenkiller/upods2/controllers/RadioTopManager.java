package com.chickenkiller.upods2.controllers;

import com.chickenkiller.upods2.interfaces.INetworkUIupdater;
import com.chickenkiller.upods2.utils.ServerApi;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by alonzilberman on 7/11/15.
 */
public class RadioTopManager {

    public enum TopType {

        MAIN_FEATURED("main_featured"), MAIN_BANNER("main_banner");

        private String value;

        TopType(String value) {
            this.value = value;
        }

        public String getStringValue() {
            return value;
        }
    }

    private final OkHttpClient client;
    private static RadioTopManager radioTopManager;

    private RadioTopManager() {
        super();
        this.client = new OkHttpClient();
    }

    public static RadioTopManager getInstance() {
        if (radioTopManager == null) {
            radioTopManager = new RadioTopManager();
        }
        return radioTopManager;
    }

    public void loadTops(TopType topType, final INetworkUIupdater uiUpdater) {
        try {
            Request request = new Request.Builder()
                    .url(ServerApi.TOPS + topType.getStringValue())
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                    uiUpdater.updateUIFailed();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        final JSONObject jResponse = new JSONObject(response.body().string());
                        uiUpdater.updateUISuccess(jResponse);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            uiUpdater.updateUIFailed();
        }
    }
}
