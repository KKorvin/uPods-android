package com.chickenkiller.upods2.controllers.internet;

import android.content.Context;

import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.interfaces.IRequestCallback;
import com.chickenkiller.upods2.interfaces.ISimpleRequestCallback;
import com.chickenkiller.upods2.models.Episode;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by alonzilberman on 7/11/15.
 */
public class BackendManager {

    public enum TopType {

        MAIN_FEATURED("main_featured"), MAIN_FEATURED_RU("main_featured_ru"), MAIN_BANNER("main_banner"),
        MAIN_PODCAST("podcasts_featured"), MAIN_PODCAST_RU("podcasts_featured_ru");

        private String name;

        TopType(String name) {
            this.name = name;
        }

        public String getStringValue() {
            return name;
        }
    }

    private static final String TAG = "BackendManager";
    private static long SIZE_OF_CACHE = 10 * 1024 * 1024; // 10 MB

    private final OkHttpClient client;
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
        File cacheDir = UpodsApplication.getContext().getDir("upods_cache", Context.MODE_PRIVATE);
        Cache cache = new Cache(cacheDir, SIZE_OF_CACHE);
        this.client = new OkHttpClient.Builder()
                .cache(cache).addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        if (GlobalUtils.isInternetConnected()) {
                            request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build();
                        } else {
                            request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
                        }
                        return chain.proceed(request);
                    }
                }).build();
    }

    public static synchronized BackendManager getInstance() {
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
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    uiUpdater.onRequestFailed();
                    searchQueueNextStep();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        final JSONObject jResponse = new JSONObject(response.body().string());
                        uiUpdater.onRequestSuccessed(jResponse);
                        //SimpleCacheManager.getInstance().cacheUrlOutput(request.url().toString(), jResponse.toString());
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
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    uiUpdater.onRequestFailed();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String strResponse = response.body().string();
                        //SimpleCacheManager.getInstance().cacheUrlOutput(request.url().toString(), strResponse);
                        uiUpdater.onRequestSuccessed(strResponse);
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
        Request request = new Request.Builder()
                .url(topLink + topType.getStringValue())
                .build();
        sendRequest(request, uiUpdater);
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

    public JSONObject sendSynchronicRequest(Request request) throws IOException, JSONException {
        Response response = client.newCall(request).execute();
        final JSONObject jResponse = new JSONObject(response.body().string());
        return jResponse;
    }

    public String sendSimpleSynchronicRequest(Request request) throws IOException, JSONException {
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public void clearSearchQueue() {
        searchQueue.clear();
    }

    public ArrayList<Episode> fetchEpisodes(String feedUrl, Podcast podcast) {
        try {
            Request request = new Request.Builder().url(feedUrl).build();
            String response = BackendManager.getInstance().sendSimpleSynchronicRequest(request);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            EpisodesXMLHandler episodesXMLHandler = new EpisodesXMLHandler();
            xr.setContentHandler(episodesXMLHandler);

            //TODO could be encoding problem
            InputSource inputSource = new InputSource(new StringReader(response));
            xr.parse(inputSource);
            ArrayList<Episode> parsedEpisodes = episodesXMLHandler.getParsedEpisods();
            if (podcast != null) {
                podcast.setDescription(episodesXMLHandler.getPodcastSummary());
            }
            return parsedEpisodes;
        } catch (Exception e) {
            Logger.printError(TAG, "Can't fetch episodes for url: " + feedUrl);
            e.printStackTrace();
            return new ArrayList<Episode>();
        }
    }

    public ArrayList<Episode> fetchEpisodes(String feedUrl) {
        return fetchEpisodes(feedUrl, null);
    }

}
