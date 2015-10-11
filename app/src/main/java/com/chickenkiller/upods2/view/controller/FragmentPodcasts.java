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
import com.chickenkiller.upods2.controllers.ProfileManager;
import com.chickenkiller.upods2.controllers.SmallPlayer;
import com.chickenkiller.upods2.interfaces.ICustumziedBackPress;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentPodcasts extends Fragment implements ICustumziedBackPress {

    public static final String TAG = "fragment_podcasts";
    private static final int CATEGORIES_FRAGMENT_POSITION = 3;
    private ViewPager vpPodcasts;
    private PodcastsPagesAdapter podcastsPagesAdapter;
    private TabLayout tlPodcastsTabs;
    private SmallPlayer smallPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcasts, container, false);
        podcastsPagesAdapter = new PodcastsPagesAdapter(getFragmentManager());
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

        ((IToolbarHolder) getActivity()).getToolbar().setTitle(R.string.podcasts);
        ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.podcasts_main));

        smallPlayer = new SmallPlayer(view, getActivity());

        //Set callback for ProviderProfileManager
        ProfileManager.getInstance().setOperationFinishCallback(new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                podcastsPagesAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        ProfileManager.getInstance().setOperationFinishCallback(null);
        smallPlayer.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (vpPodcasts.getCurrentItem() == CATEGORIES_FRAGMENT_POSITION) {
            FragmentMediaItemsCategories fCategories = (FragmentMediaItemsCategories) vpPodcasts.getAdapter().instantiateItem(vpPodcasts, vpPodcasts.getCurrentItem());
            if (fCategories instanceof ICustumziedBackPress) {
                return fCategories.onBackPressed();
            }
        }
        return true;
    }
}
