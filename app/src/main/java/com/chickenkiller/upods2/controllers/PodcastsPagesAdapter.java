package com.chickenkiller.upods2.controllers;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.view.controller.FragmentDownloadedPodcasts;
import com.chickenkiller.upods2.view.controller.FragmentFavoritesPodcasts;
import com.chickenkiller.upods2.view.controller.FragmentMediaItemsCategories;
import com.chickenkiller.upods2.view.controller.FragmentPodcastFeatured;

/**
 * Created by alonzilberman on 8/14/15.
 */
public class PodcastsPagesAdapter extends FragmentStatePagerAdapter {

    private final int PAGE_COUNTS = 4;

    public PodcastsPagesAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        Fragment podcastFragment = null;
        switch (position) {
            case 0:
                podcastFragment = new FragmentPodcastFeatured();
                break;
            case 1:
                podcastFragment = new FragmentFavoritesPodcasts();
                break;
            case 2:
                podcastFragment = new FragmentDownloadedPodcasts();
                break;
            case 3:
                podcastFragment = new FragmentMediaItemsCategories();
                break;
        }

        return podcastFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return UpodsApplication.getContext().getString(R.string.page_featured);
            case 1:
                return UpodsApplication.getContext().getString(R.string.page_favorites);
            case 2:
                return UpodsApplication.getContext().getString(R.string.page_downloaded);
            case 3:
                return UpodsApplication.getContext().getString(R.string.page_categories);
        }
        return "";
    }



    @Override
    public int getCount() {
        return PAGE_COUNTS;
    }
}
