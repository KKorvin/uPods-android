package com.chickenkiller.upods2.controllers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.app.DownloadManager.Request;

import com.chickenkiller.upods2.models.Episod;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.GlobalUtils;

/**
 * Created by alonzilberman on 10/1/15.
 */
public class DownloadManager {

    public static final String PODCASTS_DOWNLOAD_DIRECTORY = "/airtune/";
    private static DownloadManager downloadManager;

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        if (downloadManager == null) {
            downloadManager = new DownloadManager();
        }
        return downloadManager;
    }

    public void DownloadEpisod(Episod episod, Podcast podcast) {
        DownloadManager downloadManager = (DownloadManager) UpodsApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri episodUri = Uri.parse(episod.getAudeoUrl());
        Request request = new Request(episodUri);
        request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
        request.setTitle(episod.getTitle());
        request.setDescription(episod.getSummary());
        String episodName = GlobalUtils.getCleanFileName(episod.getTitle()) + ".mp3";
        String podcastName = "/" + GlobalUtils.getCleanFileName(podcast.getName()) + "/";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, PODCASTS_DOWNLOAD_DIRECTORY + podcastName + episodName);
    }
}
