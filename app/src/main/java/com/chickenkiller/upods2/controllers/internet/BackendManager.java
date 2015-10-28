package com.chickenkiller.upods2.controllers.internet;

import android.util.Log;

import com.chickenkiller.upods2.controllers.app.SimpleCacheManager;
import com.chickenkiller.upods2.interfaces.IRequestCallback;
import com.chickenkiller.upods2.interfaces.ISimpleRequestCallback;
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

    private static final String TAG = "BackendManager";
    private final OkHttpClient client;
    private final int MAX_RETRY = 5;
    private static BackendManager backendManager;
    private ArrayList<QueueTask> searchQueue;

    private class QueueTask {
        public Request request;
        public IRequestCallback iRequestHandler;

        public QueueTask(Request request, IRequestCallback uiUpdater) {
            this.request = request;
            this.iRequestHandler = uiUpdater;
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
    private void sendRequest(final Request request, final IRequestCallback uiUpdater) {
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                    uiUpdater.onRequestFailed();
                    searchQueueNextStep();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        final JSONObject jResponse = new JSONObject(response.body().string());
                        uiUpdater.onRequestSuccessed(jResponse);
                        SimpleCacheManager.getInstance().cacheUrlOutput(request.urlString(), jResponse.toString());
                        searchQueueNextStep();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            uiUpdater.onRequestFailed();
            searchQueueNextStep();
        }
    }

    private void sendRequest(final Request request, final ISimpleRequestCallback uiUpdater) {
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                    uiUpdater.onRequestFailed();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        uiUpdater.onRequestSuccessed(response.body().string());
                        SimpleCacheManager.getInstance().cacheUrlOutput(request.urlString(), response.body().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            uiUpdater.onRequestFailed();
        }
    }


    private void sendRequest(QueueTask queueTask) {
        sendRequest(queueTask.request, queueTask.iRequestHandler);
    }

    private void searchQueueNextStep() {
        if (!searchQueue.isEmpty()) {
            searchQueue.remove(searchQueue.get(0));
            if (!searchQueue.isEmpty()) {
                sendRequest(searchQueue.get(0));
            }
        }
    }

    /**
     * Simple HTTP GET request
     *
     * @param url       - any url
     * @param uiUpdater ISimpleRequestHandler
     */
    public void sendRequest(String url, final ISimpleRequestCallback uiUpdater) {
        Request request = new Request.Builder().url(url).build();
        sendRequest(request, uiUpdater);
    }

    /**
     * Simple HTTP GET request
     *
     * @param url       - any url
     * @param uiUpdater IRequestHandler
     */
    public void sendRequest(String url, final IRequestCallback uiUpdater) {
        Request request = new Request.Builder().url(url).build();
        sendRequest(request, uiUpdater);
    }

    public void loadTops(TopType topType, String topLink, final IRequestCallback uiUpdater) {
        final Request request = new Request.Builder()
                .url(topLink + topType.getStringValue())
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String fromCache = SimpleCacheManager.getInstance().readFromCache(request.urlString());
                    if (fromCache != null) {
                        final JSONObject jResponse = new JSONObject(fromCache);
                        uiUpdater.onRequestSuccessed(jResponse);
                    } else {
                        sendRequest(request, uiUpdater);
                    }
                } catch (Exception e) {
                    Log.i(TAG, "Can't restore cache for url: " + request.urlString());
                    e.printStackTrace();
                    sendRequest(request, uiUpdater);
                }
            }
        }).run();
    }

    public void doSearch(String query, final IRequestCallback uiUpdater) {
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
