package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.activity.ActivityMain;
import com.chickenkiller.upods2.controllers.adaperts.MediaPagesAdapter;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.player.SmallPlayer;
import com.chickenkiller.upods2.interfaces.ICustumziedBackPress;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.utils.enums.MediaItemType;
import com.chickenkiller.upods2.views.MediaViewpager;

import java.util.Calendar;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentMediaItemsGrid extends Fragment implements ICustumziedBackPress {

    private static final int CATEGORIES_FRAGMENT_POSITION = 3;
    public static final String LAST_ITEM_POSITION = "vp_last_postion";

    public static final String TAG;
    private MediaViewpager vpMedia;
    private MediaPagesAdapter mediaPagesAdapter;
    private TabLayout vpMediaTabs;
    private SmallPlayer smallPlayer;
    private MediaItemType mediaItemType;

    private int startItemNumber = 0;

    static {
        long time = Calendar.getInstance().get(Calendar.MILLISECOND);
        TAG = "f_media_grid" + String.valueOf(time);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_grid, container, false);
        mediaPagesAdapter = new MediaPagesAdapter(getChildFragmentManager(), mediaItemType);
        vpMedia = (MediaViewpager) view.findViewById(R.id.vpMedia);
        vpMedia.setAdapter(mediaPagesAdapter);
        vpMediaTabs = (TabLayout) view.findViewById(R.id.tlMediaTabs);
        vpMediaTabs.setBackgroundResource(R.color.color_primary);

        Toolbar toolbar = ((IToolbarHolder) getActivity()).getToolbar();
        toolbar.setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.action_search).setVisibility(View.VISIBLE);

        if (mediaItemType == MediaItemType.RADIO) {
            vpMediaTabs.setTabMode(TabLayout.MODE_FIXED);
            vpMedia.setPagingEnabled(false);
            toolbar.setTitle(R.string.radio_main);
            ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.radio_main));
            ActivityMain.lastFragmentType = MediaItemType.RADIO.ordinal();
        } else {
            ActivityMain.lastFragmentType = MediaItemType.PODCAST.ordinal();
            toolbar.setTitle(R.string.podcasts);
            ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.podcasts_main));
        }

        //Tabs color
        //tlPodcastsTabs.setTabTextColors(R.color.white_material, R.color.viewPagerNotSelectedWhite);

        // Workaround for Google's bugs
        // See https://code.google.com/p/android/issues/detail?id=180462

        vpMediaTabs.post(new Runnable() {
            @Override
            public void run() {
                vpMediaTabs.setupWithViewPager(vpMedia);
            }
        });

        smallPlayer = new SmallPlayer(view, getActivity());

        vpMedia.setCurrentItem(startItemNumber);

        return view;
    }

    public void setMediaItemType(MediaItemType mediaItemType) {
        this.mediaItemType = mediaItemType;
    }

    public void setStartItemNumber(int startItemNumber) {
        this.startItemNumber = startItemNumber;
    }

    @Override
    public void onDestroy() {
        if (smallPlayer != null) {
            smallPlayer.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        //Set callback for ProviderProfileManager
        ProfileManager.getInstance().setProfileSavedCallback(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                if (isAdded() && mediaPagesAdapter != null && data != null) {
                    mediaPagesAdapter.notifyChangesInFragments((ProfileManager.ProfileUpdateEvent) data);
                }
            }
        });
        if (smallPlayer != null) {
            smallPlayer.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (smallPlayer != null) {
            smallPlayer.onPause();
        }
        ActivityMain.lastChildFragmentNumber = vpMedia.getCurrentItem();
        ProfileManager.getInstance().setProfileSavedCallback(null);
        super.onPause();
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = (Fragment) vpMedia.getAdapter().instantiateItem(vpMedia, vpMedia.getCurrentItem());
        if (fragment instanceof ICustumziedBackPress) {
            return ((ICustumziedBackPress) fragment).onBackPressed();
        }
        return true;
    }
}
