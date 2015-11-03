package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.MediaItemsAdapter;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IUpdateableFragment;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.MediaItemTitle;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.utils.enums.MediaItemType;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentMediaItemsList extends Fragment implements IUpdateableFragment {

    public static final String TAG;
    private RecyclerView rvMediaItems;
    private MediaItemsAdapter mediaItemsAdapter;
    private MediaItemType mediaItemType;

    static {
        long time = Calendar.getInstance().get(Calendar.MILLISECOND);
        TAG = "f_favorites_downloaded_" + String.valueOf(time);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horizontal_media_items, container, false);
        rvMediaItems = (RecyclerView) view.findViewById(R.id.rvMediaItems);

        ArrayList<MediaItem> allItems = new ArrayList<>();
        MediaItemTitle mediaItemTitle = null;

        if (mediaItemType == MediaItemType.PODCAST_DOWNLOADED || mediaItemType == null) {
            mediaItemTitle = new MediaItemTitle(getString(R.string.recently_downloaded));
            mediaItemTitle.showButton = false;
            allItems.add(mediaItemTitle);
            ArrayList<Podcast> downloadedPodcasts = ProfileManager.getInstance().getDownloadedPodcasts();
            allItems.addAll(downloadedPodcasts);
        } else if (mediaItemType == MediaItemType.PODCAST_FAVORITE) {
            mediaItemTitle = new MediaItemTitle(getString(R.string.recently_subscribed));
            mediaItemTitle.showButton = false;
            allItems.add(mediaItemTitle);
            ArrayList<Podcast> favoritePodcasts = ProfileManager.getInstance().getSubscribedPodcasts();
            allItems.addAll(favoritePodcasts);
        } else if (mediaItemType == MediaItemType.RADIO_SUBSCRIBED) {
            mediaItemTitle = new MediaItemTitle(getString(R.string.recently_subscribed));
            mediaItemTitle.showButton = false;
            allItems.add(mediaItemTitle);
            ArrayList<RadioItem> subscribedRadioItems = ProfileManager.getInstance().getSubscribedRadioItems();
            allItems.addAll(subscribedRadioItems);
        } else if (mediaItemType == MediaItemType.RADIO_RECENT) {
            mediaItemTitle = new MediaItemTitle(getString(R.string.recently_played));
            mediaItemTitle.showButton = false;
            allItems.add(mediaItemTitle);
            ArrayList<RadioItem> recentRadioItems = ProfileManager.getInstance().getRecentRadioItems();
            allItems.addAll(recentRadioItems);
        }

        mediaItemsAdapter = new MediaItemsAdapter(getActivity(), R.layout.card_media_item_horizontal, R.layout.media_item_title, allItems);
        mediaItemsAdapter.setFragmentsManager((IFragmentsManager) getActivity());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        rvMediaItems.setLayoutManager(layoutManager);
        rvMediaItems.setAdapter(mediaItemsAdapter);
        return view;
    }

    public void setMediaItemType(MediaItemType mediaItemType) {
        this.mediaItemType = mediaItemType;
    }

    @Override
    public void update() {
        ArrayList<MediaItem> allItems = new ArrayList<>();
        allItems.add(mediaItemsAdapter.getItemAt(0));
        if (mediaItemType == MediaItemType.PODCAST_DOWNLOADED || mediaItemType == null) {
            ArrayList<Podcast> downloadedPodcasts = ProfileManager.getInstance().getDownloadedPodcasts();
            allItems.addAll(downloadedPodcasts);
        } else if (mediaItemType == MediaItemType.PODCAST_FAVORITE) {
            ArrayList<Podcast> favoritePodcasts = ProfileManager.getInstance().getSubscribedPodcasts();
            allItems.addAll(favoritePodcasts);
        } else if (mediaItemType == MediaItemType.RADIO_SUBSCRIBED) {
            ArrayList<RadioItem> subscribedRadioItems = ProfileManager.getInstance().getSubscribedRadioItems();
            allItems.addAll(subscribedRadioItems);
        } else if (mediaItemType == MediaItemType.RADIO_RECENT) {
            ArrayList<RadioItem> recentRadioItems = ProfileManager.getInstance().getRecentRadioItems();
            allItems.addAll(recentRadioItems);
        }
        mediaItemsAdapter.clearItems();
        mediaItemsAdapter.addItems(allItems);
        mediaItemsAdapter.notifyDataSetChanged();
    }
}
