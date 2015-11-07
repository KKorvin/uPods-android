package com.chickenkiller.upods2.controllers.player;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;

import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.utils.Logger;

import java.util.HashMap;

/**
 * Created by alonzilberman on 11/7/15.
 */
public class MetaDataFetcher extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = "MetaDataFetcher";
    private IOperationFinishWithDataCallback onDataFetched;
    private String streamUrl;


    public MetaDataFetcher(IOperationFinishWithDataCallback onDataFetched, String streamUrl) {
        this.onDataFetched = onDataFetched;
        this.streamUrl = streamUrl;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Uri steamUri = Uri.parse(streamUrl);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(streamUrl,new HashMap<String, String>());
        String bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        Logger.printInfo(LOG_TAG, bitrate);
        return null;
    }


}
