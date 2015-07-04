package com.chickenkiller.upods2.view.controller;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.SlidinMenuAdapter;
import com.chickenkiller.upods2.models.SlidingMenuItem;

/**
 * Created by alonzilberman on 7/4/15.
 */
public class SlidingMenu {

    private Activity activity;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    public View.OnClickListener exitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            activity.finish();
        }
    };


    public SlidingMenu(Activity mActivity, Toolbar mToolbar) {
        this.activity = mActivity;
        this.mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        this.mDrawerList = (ListView) activity.findViewById(R.id.left_drawer);
        this.mDrawerList.setAdapter(new SlidinMenuAdapter(activity, SlidingMenuItem.fromDefaultSlidingMenuSet(activity)));
        this.mDrawerToggle = new ActionBarDrawerToggle(activity, mDrawerLayout,
                mToolbar,
                R.string.app_name, R.string.app_name) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                activity.invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                activity.invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    public void toogle() {
        if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

}
