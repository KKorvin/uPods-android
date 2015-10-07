package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.MediaItemsAdapter;
import com.chickenkiller.upods2.controllers.ProfileManager;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;

import java.util.ArrayList;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentDownloadedPodcasts extends Fragment {

    public static final String TAG = "fragment_favorites_downloaded";
    private RecyclerView rvMediaItems;
    private MediaItemsAdapter mediaItemsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horizontal_media_items, container, false);
        rvMediaItems = (RecyclerView) view.findViewById(R.id.rvMediaItems);

        MediaItemTitle mediaItemTitle = new MediaItemTitle(getString(R.string.recently_downloaded));
        mediaItemTitle.showButton = false;
        ArrayList<MediaItem> allItems = new ArrayList<>();
        allItems.add(mediaItemTitle);
        allItems.addAll(ProfileManager.getInstance().getDownloadedPodcasts());
        mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item_horizontal, R.layout.media_item_title, allItems);
        mediaItemsAdapter.setFragmentsManager((IFragmentsManager) getActivity());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        rvMediaItems.setLayoutManager(layoutManager);
        rvMediaItems.setAdapter(mediaItemsAdapter);
        return view;
    }

}
