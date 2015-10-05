package com.chickenkiller.upods2.controllers;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.chickenkiller.upods2.interfaces.IContentLoadListener;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IPlayableTrack;
import com.chickenkiller.upods2.interfaces.IUIProgressUpdater;
import com.chickenkiller.upods2.utils.GlobalUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alonzilberman on 10/1/15.
 */
public class DownloadMaster {

    public static final String PODCASTS_DOWNLOAD_DIRECTORY = "/airtune";

    private static final String DM_LOG = "DownloadMaster";
    private static final long PRGRESS_UPDATE_RATE = 500;
    private static DownloadMaster downloadMaster;
    private ArrayList<DownloadTask> allTasks;
    private TimerTask progressUpdateTask;

    public static class DownloadTask {
        public IPlayableMediaItem mediaItem;
        public IPlayableTrack track;
        public long downloadId;
        public IUIProgressUpdater progressUpdater;
        public IContentLoadListener contentLoadListener;

        DownloadTask() {
            this.downloadId = 0;
            this.mediaItem = null;
            this.track = null;
            this.progressUpdater = null;
            this.contentLoadListener = null;
        }
    }

    private DownloadMaster() {
        this.allTasks = new ArrayList<>();
    }

    public static DownloadMaster getInstance() {
        if (downloadMaster == null) {
            downloadMaster = new DownloadMaster();
        }
        return downloadMaster;
    }

    public void download(DownloadTask task) {
        DownloadManager downloadManager = (DownloadManager) UpodsApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri episodUri = Uri.parse(task.track.getAudeoUrl());
        Request request = new Request(episodUri);
        request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
        request.setTitle(task.track.getTitle());
        request.setDescription(task.track.getSubTitle());
        String trackName = GlobalUtils.getCleanFileName(task.track.getTitle()) + ".mp3";
        String mediaItemName = "/" + GlobalUtils.getCleanFileName(task.mediaItem.getName());
        String finalPath = PODCASTS_DOWNLOAD_DIRECTORY+mediaItemName;
        request.setDestinationInExternalPublicDir(finalPath, trackName);
        task.downloadId = downloadManager.enqueue(request);
        allTasks.add(task);
        Log.i(DM_LOG, "Starting download episod " + trackName + " to "  + finalPath);
        runProgressUpdater();
    }

    private void runProgressUpdater() {
        if (progressUpdateTask == null && allTasks.size() > 0) {
            progressUpdateTask = new TimerTask() {
                @Override
                public void run() {
                    if (allTasks.size() == 0) {
                        progressUpdateTask.cancel();
                        progressUpdateTask = null;
                    } else {
                        for (DownloadTask task : allTasks) {
                            if (task.progressUpdater != null) {
                                task.progressUpdater.updateProgressUI(getDownloadProgress(task.track.getTitle()));
                            }
                        }
                    }
                }
            };
            new Timer().scheduleAtFixedRate(progressUpdateTask, 0, PRGRESS_UPDATE_RATE);
        }
    }

    private DownloadTask getTaskByName(String itemName) {
        for (DownloadTask task : allTasks) {
            if (task.track.getTitle().equals(itemName)) {
                return task;
            }
        }
        return null;
    }

    private DownloadTask getTaskById(long downloadId) {
        for (DownloadTask task : allTasks) {
            if (task.downloadId == downloadId) {
                return task;
            }
        }
        return null;
    }

    private double getDownloadProgress(String itemName) {
        DownloadTask task = getTaskByName(itemName);
        double progress = 0.0;
        if (task != null) {
            long taskId = task.downloadId;
            DownloadManager downloadManager = (DownloadManager) UpodsApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(taskId);
            Cursor c = downloadManager.query(query);

            if (c.moveToFirst()) {
                int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                long size = c.getInt(sizeIndex);
                long downloaded = c.getInt(downloadedIndex);
                if (size != -1) progress = downloaded * 100.0 / size;
            }
            c.close();
        }
        return progress;
    }

    public boolean isItemDownloading(String itemName) {
        return getTaskByName(itemName) != null;
    }

    /**
     * Removes all UI updatets
     */
    public void cleanProgressInterfaces() {
        for (DownloadTask task : allTasks) {
            task.progressUpdater = null;
        }
    }

    public void cancelDownload(String itemName) {
        DownloadManager downloadManager = (DownloadManager) UpodsApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadTask task = getTaskByName(itemName);
        downloadManager.remove(task.downloadId);
        allTasks.remove(task);
        Log.i(DM_LOG, "Task for item name: " + itemName + " canceled ");
    }

    /**
     * Should be called when DownloadManager finishes to download item
     */
    public void markDownloadTaskFinished(long downloadId) {
        DownloadManager downloadManager = (DownloadManager) UpodsApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            DownloadTask task = getTaskById(downloadId);
            int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (task != null) {
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    ProfileManager.getInstance().addDownloadedTrack(task.mediaItem, task.track);
                    task.contentLoadListener.onContentLoaded();
                }
                allTasks.remove(task);
            }
        }
        c.close();
    }
}
