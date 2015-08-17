package com.chickenkiller.upods2.activity;

import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IOverlayable;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.utils.UIHelper;
import com.chickenkiller.upods2.view.controller.FragmentMainFeatured;
import com.chickenkiller.upods2.view.controller.FragmentSearch;
import com.chickenkiller.upods2.view.controller.FragmentWellcome;
import com.chickenkiller.upods2.view.controller.SlidingMenu;

public class ActivityMain extends FragmentsActivity implements IOverlayable, IToolbarHolder {


    private static final float MAX_OVERLAY_LEVEL = 0.8f;
    private static final int FRAGMENT_TRANSACTION_TIME = 300;
    private static final int WELLCOME_SCREEN_TIME = 2000;
    private static boolean isFirstRun = true;
    private Toolbar toolbar;
    private SlidingMenu slidingMenu;
    private View vOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vOverlay = findViewById(R.id.vOverlay);

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.inflateMenu(R.menu.menu_activity_main);
        toolbar.setTitle(R.string.radio_main);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        MenuItem searchMenuItem = toolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        UIHelper.changeSearchViewTextColor(searchView, Color.WHITE);

        slidingMenu = new SlidingMenu(this, toolbar);
        showFragment(R.id.fl_content, new FragmentWellcome(), FragmentWellcome.TAG);

        if (isFirstRun) {
            toolbar.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    toolbar.setVisibility(View.VISIBLE);
                    showFragment(R.id.fl_content, new FragmentMainFeatured(), FragmentMainFeatured.TAG);
                }
            }, WELLCOME_SCREEN_TIME);
        } else {
            toolbar.setVisibility(View.VISIBLE);
            showFragment(R.id.fl_content, new FragmentMainFeatured(), FragmentMainFeatured.TAG);
        }

        isFirstRun = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //TODO change
        slidingMenu.getAdapter().clearRowSelections();
        slidingMenu.getAdapter().notifyDataSetChanged();

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            if (getLatestFragmentTag().equals(FragmentMainFeatured.TAG)) {
                finish();
            } else if (getLatestFragmentTag().equals(FragmentSearch.TAG)) {
                toolbar.getMenu().findItem(R.id.action_search).collapseActionView();
                getFragmentManager().popBackStack();
            } else {
                if (isOverlayShown()) {
                    toggleOverlay();
                }
                getFragmentManager().popBackStack();
            }
        } else {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            super.onBackPressed();
        }

    }

    @Override
    public void toggleOverlay() {
        ObjectAnimator alphaAnimation;
        if (isOverlayShown()) {
            alphaAnimation = ObjectAnimator.ofFloat(vOverlay, View.ALPHA, MAX_OVERLAY_LEVEL, 0);
        } else {
            alphaAnimation = ObjectAnimator.ofFloat(vOverlay, View.ALPHA, 0, MAX_OVERLAY_LEVEL);
        }
        alphaAnimation.setDuration(FRAGMENT_TRANSACTION_TIME);
        alphaAnimation.start();
    }

    @Override
    public boolean isOverlayShown() {
        return vOverlay.getAlpha() != 0;
    }

    @Override
    public void setOverlayAlpha(int alphaPercent) {
        vOverlay.getBackground().setAlpha(alphaPercent);
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

}
