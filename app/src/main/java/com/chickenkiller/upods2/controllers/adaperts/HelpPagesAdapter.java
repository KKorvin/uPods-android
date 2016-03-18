package com.chickenkiller.upods2.controllers.adaperts;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.View;

import com.chickenkiller.upods2.fragments.FragmentHelpItem;
import com.chickenkiller.upods2.utils.Logger;

/**
 * Created by alonzilberman on 8/14/15.
 * Use it for simple help viewpager
 */
public class HelpPagesAdapter extends FragmentStatePagerAdapter {

    private final int HELP_PAGES_COUNT = 4;
    private View.OnClickListener closeClickListener;

    public HelpPagesAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        Fragment currentFragment = new FragmentHelpItem();
        ((FragmentHelpItem) currentFragment).setIndex(position);
        Logger.printInfo("position", String.valueOf(position));
        if (position == HELP_PAGES_COUNT - 1) {
            ((FragmentHelpItem) currentFragment).setCloseClickListener(closeClickListener);
        }
        return currentFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        return title;
    }

    @Override
    public int getCount() {
        return HELP_PAGES_COUNT;
    }

    public void setCloseClickListener(View.OnClickListener closeClickListener) {
        this.closeClickListener = closeClickListener;
    }
}
