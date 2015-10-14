package com.chickenkiller.upods2.view.controller;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.Track;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class DialogFragmentTrackInfo extends DialogFragment {

    public static final String TAG = "df_track_info";
    private Track track;
    private WebView wbTrackInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_track_info, container, false);
        wbTrackInfo = (WebView) view.findViewById(R.id.wbTrackInfo);
        wbTrackInfo.loadData(track.getInfo(), "text/html", "utf-8");
        getDialog().setTitle(R.string.episod_notes);
        return view;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
