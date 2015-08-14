package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chickenkiller.upods2.R;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentPodcastsPage extends Fragment {

    public static final String TAG = "fragment_podcasts_page";
    private String testPodcast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_podcasts, container, false);
        TextView tvTitle = (TextView) view.findViewById(R.id.tvPodcastTitle);
        tvTitle.setText(testPodcast);
        return view;
    }

    public void setTestPodcast(String str) {
        this.testPodcast = str;
    }
}
