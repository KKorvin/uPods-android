package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.MediaPagesAdapter;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.player.SmallPlayer;
import com.chickenkiller.upods2.interfaces.ICustumziedBackPress;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.utils.DataHolder;
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

        if (mediaItemType == MediaItemType.RADIO) {
            vpMediaTabs.setTabMode(TabLayout.MODE_FIXED);
            vpMedia.setPagingEnabled(false);
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

        if (mediaItemType == MediaItemType.PODCAST) {
            ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.podcasts);
            ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.podcasts_main));
        } else {
            ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.radio_main);
            ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.radio_main));
        }

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
        ProfileManager.getInstance().setOperationFinishCallback(new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                if (isAdded() && mediaPagesAdapter != null) {
                    mediaPagesAdapter.notifyDataSetChanged();
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
        DataHolder.getInstance().save(LAST_ITEM_POSITION, vpMedia.getCurrentItem());
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
