package com.chickenkiller.upods2.controllers.player;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.utils.Logger;


import java.util.HashMap;

/**
 * Created by alonzilberman on 11/7/15.
 */
public class MetaDataFetcher extends AsyncTask<Void, Void, Void> {

    public static class MetaData {
        public String bitrate;
        public String format;

        public MetaData() {

        }
    }

    private static final String LOG_TAG = "MetaDataFetcher";
    private IOperationFinishWithDataCallback onDataFetched;
    private String streamUrl;
    private MetaData metaData;

    public MetaDataFetcher(IOperationFinishWithDataCallback onDataFetched, String streamUrl) {
        this.onDataFetched = onDataFetched;
        this.streamUrl = streamUrl;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(streamUrl, new HashMap<String, String>());
            metaData = new MetaData();
            metaData.bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE) == null ? "" : mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            metaData.format = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == null ? "" : mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            Logger.printInfo(LOG_TAG, "Secsessfuly fetch metadata");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.printInfo(LOG_TAG, "Failed to fetch metadata");
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        if (metaData != null && onDataFetched != null) {
            this.onDataFetched.operationFinished(metaData);
        }
        super.onPostExecute(aVoid);
    }
}
