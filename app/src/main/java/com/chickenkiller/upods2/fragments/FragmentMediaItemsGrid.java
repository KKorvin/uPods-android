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
import com.chickenkiller.upods2.utils.MediaItemType;
import com.chickenkiller.upods2.views.MediaViewpager;

import java.util.Calendar;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentMediaItemsGrid extends Fragment implements ICustumziedBackPress {

    public static final String TAG;
    private static final int CATEGORIES_FRAGMENT_POSITION = 3;
    private MediaViewpager vpMedia;
    private MediaPagesAdapter mediaPagesAdapter;
    private TabLayout vpMediaTabs;
    private SmallPlayer smallPlayer;
    private MediaItemType mediaItemType;

    static {
        long time = Calendar.getInstance().get(Calendar.MILLISECOND);
        TAG = "f_media_grid" + String.valueOf(time);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_grid, container, false);
        mediaPagesAdapter = new MediaPagesAdapter(getFragmentManager(), mediaItemType);
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

        //Set callback for ProviderProfileManager
        ProfileManager.getInstance().setOperationFinishCallback(new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                mediaPagesAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    public void setMediaItemType(MediaItemType mediaItemType) {
        this.mediaItemType = mediaItemType;
    }

    @Override
    public void onDestroy() {
        ProfileManager.getInstance().setOperationFinishCallback(null);
        smallPlayer.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (mediaItemType == MediaItemType.PODCAST && vpMedia.getCurrentItem() == CATEGORIES_FRAGMENT_POSITION) {
            FragmentMediaItemsCategories fCategories = (FragmentMediaItemsCategories) vpMedia.getAdapter().instantiateItem(vpMedia, vpMedia.getCurrentItem());
            if (fCategories instanceof ICustumziedBackPress) {
                return fCategories.onBackPressed();
            }
        } else if (mediaItemType == MediaItemType.RADIO) {
            getActivity().finish();
        }
        return true;
    }
}
