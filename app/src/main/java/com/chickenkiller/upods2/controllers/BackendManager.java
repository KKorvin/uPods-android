package com.chickenkiller.upods2.controllers;

import com.chickenkiller.upods2.interfaces.INetworkUIupdater;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by alonzilberman on 7/11/15.
 */
public class BackendManager {

    public enum TopType {

        MAIN_FEATURED("main_featured"), MAIN_BANNER("main_banner"), MAIN_PODCAST("podcasts_featured");

        private String name;

        TopType(String name) {
            this.name = name;
        }

        public String getStringValue() {
            return name;
        }
    }

    private final OkHttpClient client;
    private final int MAX_RETRY = 5;
    private static BackendManager backendManager;
    private ArrayList<QueueTask> searchQueue;

    private class QueueTask {
        public Request request;
        public INetworkUIupdater iNetworkUIupdater;

        public QueueTask(Request request, INetworkUIupdater uiUpdater) {
            this.request = request;
            this.iNetworkUIupdater = uiUpdater;
        }
    }

    private BackendManager() {
        super();
        this.client = new OkHttpClient();
    }

    public static BackendManager getInstance() {
        if (backendManager == null) {
            backendManager = new BackendManager();
            backendManager.searchQueue = new ArrayList<>();
        }
        return backendManager;
    }

    /**
     * Wrapper for simple backend queries
     *
     * @param request   - OKHttp request
     * @param uiUpdater - INetworkUIupdater to update UI
     */
    private void sendRequest(final Request request, final INetworkUIupdater uiUpdater) {
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                    uiUpdater.updateUIFailed();
                    searchQueueNextStep();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        final JSONObject jResponse = new JSONObject(response.body().string());
                        uiUpdater.updateUISuccess(jResponse);
                        searchQueueNextStep();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            uiUpdater.updateUIFailed();
            searchQueueNextStep();
        }
    }

    private void sendRequest(QueueTask queueTask) {
        sendRequest(queueTask.request, queueTask.iNetworkUIupdater);
    }

    private void searchQueueNextStep() {
        if (!searchQueue.isEmpty()) {
            searchQueue.remove(searchQueue.get(0));
            if (!searchQueue.isEmpty()) {
                sendRequest(searchQueue.get(0));
            }
        }
    }

    public void loadTops(TopType topType, String topLink, final INetworkUIupdater uiUpdater) {
        Request request = new Request.Builder()
                .url(topLink + topType.getStringValue())
                .build();
        sendRequest(request, uiUpdater);
    }

    public void doSearch(String query, final INetworkUIupdater uiUpdater) {
        Request request = new Request.Builder()
                .url(query)
                .build();
        if (searchQueue.isEmpty()) {
            searchQueue.add(new QueueTask(request, uiUpdater));
            sendRequest(request, uiUpdater);
        } else {
            searchQueue.add(new QueueTask(request, uiUpdater));
        }
    }

    public void clearSearchQueue() {
        searchQueue.clear();
    }
}
