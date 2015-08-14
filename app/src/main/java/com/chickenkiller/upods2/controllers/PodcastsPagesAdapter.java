package com.chickenkiller.upods2.controllers;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.view.controller.FragmentPodcastsPage;

/**
 * Created by alonzilberman on 8/14/15.
 */
public class PodcastsPagesAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNTS = 4;
    private Context mContext;

    public PodcastsPagesAdapter(FragmentManager fm, Context mContext) {
        super(fm);
        this.mContext = mContext;
    }

    @Override
    public Fragment getItem(int position) {
        FragmentPodcastsPage fragmentPagePodcasts = new FragmentPodcastsPage();
        fragmentPagePodcasts.setTestPodcast(String.valueOf(position));
        return fragmentPagePodcasts;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.page_featured);
            case 1:
                return mContext.getString(R.string.page_favorites);
            case 2:
                return mContext.getString(R.string.page_downloaded);
            case 3:
                return mContext.getString(R.string.page_categories);
        }
        return "";
    }

    @Override
    public int getCount() {
        return PAGE_COUNTS;
    }
}
