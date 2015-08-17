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
public class RadioBackendManager {

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
    private static RadioBackendManager radioBackendManager;

    private RadioBackendManager() {
        super();
        this.client = new OkHttpClient();
    }

    public static RadioBackendManager getInstance() {
        if (radioBackendManager == null) {
            radioBackendManager = new RadioBackendManager();
        }
        return radioBackendManager;
    }

    /**
     * Wrapper for simple backend queries
     *
     * @param request - OKHttp request
     * @param uiUpdater - INetworkUIupdater to update UI
     */
    private void sendRequest(Request request, final INetworkUIupdater uiUpdater) {
        try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            uiUpdater.updateUIFailed();
        }
    }

    public void loadTops(TopType topType, final INetworkUIupdater uiUpdater) {
        Request request = new Request.Builder()
                .url(ServerApi.TOPS + topType.getStringValue())
                .build();
        sendRequest(request, uiUpdater);
    }

    public void doSearch(String query, final INetworkUIupdater uiUpdater) {
        Request request = new Request.Builder()
                .url(ServerApi.RADIO_SEARCH + query)
                .build();
        sendRequest(request, uiUpdater);
    }
}
