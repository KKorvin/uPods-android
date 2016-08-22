package com.chickenkiller.upods2.controllers.adaperts;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.fragments.FragmentMainFeatured;
import com.chickenkiller.upods2.fragments.FragmentMediaItemsCategories;
import com.chickenkiller.upods2.fragments.FragmentMediaItemsList;
import com.chickenkiller.upods2.fragments.FragmentPodcastFeatured;
import com.chickenkiller.upods2.utils.enums.MediaItemType;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 * Created by Alon Zilberman on 8/14/15.
 * Use it for global ViewPagers with media items
 */
public class MediaPagesAdapter extends FragmentStatePagerAdapter {

    private final int PODCASTS_PAGES_COUNT = 4;
    private final int RADIO_PAGES_COUNT = 3;
    private MediaItemType mediaItemType;
    private int pagesCount;

    private Hashtable<Integer, WeakReference<Fragment>> fragmentReferences;


    public MediaPagesAdapter(FragmentManager fm, MediaItemType mediaItemType) {
        super(fm);
        this.fragmentReferences = new Hashtable<>();
        this.mediaItemType = mediaItemType;
        if (mediaItemType == MediaItemType.RADIO) {
            this.pagesCount = RADIO_PAGES_COUNT;
        } else {
            this.pagesCount = PODCASTS_PAGES_COUNT;
        }

    }


    @Override
    public Fragment getItem(int position) {
        Fragment currentFragment = null;
        if (mediaItemType == MediaItemType.RADIO) {
            currentFragment = getRadioFragment(position);
        } else {
            currentFragment = getPodcastFragment(position);
        }
        return currentFragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        fragmentReferences.put(position, new WeakReference<Fragment>(createdFragment));
        return createdFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        if (mediaItemType == MediaItemType.RADIO) {
            title = getRadioPageTitle(position);
        } else {
            title = getPodcastTitle(position);
        }

        return title;
    }

    private String getPodcastTitle(int position) {
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

    private Fragment getPodcastFragment(int position) {
        Fragment podcastFragment = null;
        switch (position) {
            case 0:
                podcastFragment = new FragmentPodcastFeatured();
                break;
            case 1:
                podcastFragment = new FragmentMediaItemsList();
                ((FragmentMediaItemsList) podcastFragment).setMediaItemType(MediaItemType.PODCAST_FAVORITE);
                break;
            case 2:
                podcastFragment = new FragmentMediaItemsList();
                ((FragmentMediaItemsList) podcastFragment).setMediaItemType(MediaItemType.PODCAST_DOWNLOADED);
                break;
            case 3:
                podcastFragment = new FragmentMediaItemsCategories();
                break;
        }
        return podcastFragment;
    }

    private Fragment getRadioFragment(int position) {
        Fragment radioFragment = null;
        switch (position) {
            case 0:
                radioFragment = new FragmentMainFeatured();
                break;
            case 1:
                radioFragment = new FragmentMediaItemsList();
                ((FragmentMediaItemsList) radioFragment).setMediaItemType(MediaItemType.RADIO_SUBSCRIBED);
                break;
            case 2:
                radioFragment = new FragmentMediaItemsList();
                ((FragmentMediaItemsList) radioFragment).setMediaItemType(MediaItemType.RADIO_RECENT);
                break;
        }
        return radioFragment;
    }

    private String getRadioPageTitle(int position) {
        switch (position) {
            case 0:
                return UpodsApplication.getContext().getString(R.string.page_featured);
            case 1:
                return UpodsApplication.getContext().getString(R.string.page_subscribed);
            case 2:
                return UpodsApplication.getContext().getString(R.string.page_recent);
        }
        return "";
    }

    @Override
    public int getCount() {
        return pagesCount;
    }

    public void notifyChangesInFragments(ProfileManager.ProfileUpdateEvent profileUpdateEvent) {
        for (int i = 0; i < getCount(); i++) {
            WeakReference<Fragment> ref = fragmentReferences.get(i);
            if (ref != null) {
                Fragment fragment = ref.get();
                if (fragment instanceof FragmentMediaItemsList) {
                    ((FragmentMediaItemsList) fragment).notifyMediaItemChanges(profileUpdateEvent);
                } else if (fragment instanceof FragmentPodcastFeatured) {
                    ((FragmentPodcastFeatured) fragment).notifyMediaItemChanges(profileUpdateEvent);
                } else if (fragment instanceof FragmentMainFeatured) {
                    ((FragmentMainFeatured) fragment).notifyMediaItemChanges(profileUpdateEvent);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void reloadAllFragmentsData() {
        for (int i = 0; i < getCount(); i++) {
            WeakReference<Fragment> ref = fragmentReferences.get(i);
            Fragment fragment = ref.get();
            if (fragment != null && fragment instanceof FragmentMediaItemsList) {
                ((FragmentMediaItemsList) fragment).reloadAllData();
            }
        }
    }
}
