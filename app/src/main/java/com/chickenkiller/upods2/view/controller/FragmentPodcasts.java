package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.PodcastsPagesAdapter;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentPodcasts extends Fragment {

    public static final String TAG = "fragment_podcasts";
    private ViewPager vpPodcasts;
    private PodcastsPagesAdapter podcastsPagesAdapter;
    private TabLayout tlPodcastsTabs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcasts, container, false);
        podcastsPagesAdapter = new PodcastsPagesAdapter(getFragmentManager(), getActivity());
        vpPodcasts = (ViewPager) view.findViewById(R.id.vpPodcasts);
        vpPodcasts.setAdapter(podcastsPagesAdapter);

        tlPodcastsTabs = (TabLayout) view.findViewById(R.id.tlPodcastsTabs);

        // Workaround for Google's bugs
        // See https://code.google.com/p/android/issues/detail?id=180462

        tlPodcastsTabs.post(new Runnable() {
            @Override
            public void run() {
                tlPodcastsTabs.setupWithViewPager(vpPodcasts);
            }
        });
        return view;
    }
}
