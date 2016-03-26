package com.chickenkiller.upods2.views;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.SlidingMenuAdapter;
import com.chickenkiller.upods2.controllers.app.LoginMaster;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.ISlidingMenuManager;
import com.chickenkiller.upods2.models.SlidingMenuHeader;
import com.chickenkiller.upods2.models.SlidingMenuItem;
import com.chickenkiller.upods2.models.SlidingMenuRow;
import com.chickenkiller.upods2.models.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonzilberman on 7/4/15.
 */
public class SlidingMenu implements ISlidingMenuManager {

    private Activity activity;
    private DrawerLayout mDrawerLayout;
    private RecyclerView rvDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private SlidingMenuAdapter slidingMenuAdapter;
    private SlidingMenuHeader slidingMenuHeader;

    private final LinearLayoutManager layoutManager;

    public SlidingMenu(Activity mActivity, Toolbar mToolbar) {
        this.activity = mActivity;
        this.layoutManager = new LinearLayoutManager(mActivity);
        this.layoutManager.setOrientation(OrientationHelper.VERTICAL);
        this.mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        this.slidingMenuHeader = new SlidingMenuHeader();

        List<SlidingMenuItem> slidingMenuItems = new ArrayList<>();
        slidingMenuItems.add(slidingMenuHeader);
        slidingMenuItems.addAll(SlidingMenuRow.fromDefaultSlidingMenuSet());
        if (activity instanceof IFragmentsManager) {
            this.slidingMenuAdapter = new SlidingMenuAdapter(slidingMenuItems,
                    R.layout.sliding_menu_item, this, (IFragmentsManager) activity);
        } else {
            this.slidingMenuAdapter = new SlidingMenuAdapter(slidingMenuItems,
                    R.layout.sliding_menu_item, this);
        }

        SlidingMenuDivider divider = new SlidingMenuDivider(activity, R.drawable.sliding_menu_divider);

        this.rvDrawerList = (RecyclerView) activity.findViewById(R.id.left_drawer);
        this.rvDrawerList.setHasFixedSize(true);
        this.rvDrawerList.setAdapter(slidingMenuAdapter);
        this.rvDrawerList.setLayoutManager(this.layoutManager);
        this.rvDrawerList.addItemDecoration(divider);

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
        mDrawerToggle.syncState();
        updateHeader(false);
    }

    public SlidingMenuAdapter getAdapter() {
        return slidingMenuAdapter;
    }

    @Override
    public void toggle() {
        if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    public void updateHeader(boolean isForceUpdate) {
        LoginMaster.getInstance().initUserProfile(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                UserProfile userProfile = (UserProfile) data;
                if (slidingMenuHeader != null && slidingMenuAdapter != null) {
                    slidingMenuHeader.setEmail(userProfile.getEmail());
                    slidingMenuHeader.setName(userProfile.getName());
                    slidingMenuHeader.setImgUrl(userProfile.getProfileImageUrl());
                    slidingMenuAdapter.notifyDataSetChanged();
                }
            }
        }, isForceUpdate);
    }

    public void updateHeader(UserProfile userProfile) {
        if (slidingMenuHeader != null && slidingMenuAdapter != null) {
            slidingMenuHeader.setEmail(userProfile.getEmail());
            slidingMenuHeader.setName(userProfile.getName());
            slidingMenuHeader.setImgUrl(userProfile.getProfileImageUrl());
            slidingMenuAdapter.notifyDataSetChanged();
        }
    }
}
