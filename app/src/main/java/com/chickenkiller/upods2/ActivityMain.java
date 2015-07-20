package com.chickenkiller.upods2;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.view.controller.FragmentMainFeatured;
import com.chickenkiller.upods2.view.controller.SlidingMenu;

public class ActivityMain extends Activity implements IFragmentsManager {


    private static final float MAX_OVERLAY_LEVEL = 0.8f;
    private static final int FRAGMENT_TRANSACTION_TIME = 500;
    private Toolbar toolbar;
    private SlidingMenu slidingMenu;
    private View vOverlay;
    private int currentMainFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vOverlay = findViewById(R.id.vOverlay);
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.inflateMenu(R.menu.menu_activity_main);
        slidingMenu = new SlidingMenu(this, toolbar);
        showFragment(R.id.ln_content, new FragmentMainFeatured(), FragmentMainFeatured.TAG);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            toggleOverlay();
        }
        else
            super.onBackPressed();
    }

    @Override
    public void showFragment(int id, Fragment fragment, String tag, FragmentOpenType openType, FragmentAnimationType animationType) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (animationType == FragmentAnimationType.BOTTOM_TOP) {
            ft.setCustomAnimations(R.animator.animation_fragment_bototm_top, R.animator.animation_fragment_top_bototm,
                    R.animator.animation_fragment_bototm_top, R.animator.animation_fragment_top_bototm);
        }
        if (openType == FragmentOpenType.OVERLAY) {
            ft.add(id, fragment, tag);
            toggleOverlay();
        } else {
            ft.replace(id, fragment);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
        currentMainFragmentId = id;
    }

    @Override
    public void showFragment(int id, Fragment fragment, String tag) {
        showFragment(id, fragment, tag, FragmentOpenType.REPLACE, FragmentAnimationType.DEFAULT);
    }

    @Override
    public int getCurrentMainFragmentId() {
        return currentMainFragmentId;
    }

    private void toggleOverlay() {
        ObjectAnimator alphaAnimation;
        if (vOverlay.getAlpha() == 0) {
            alphaAnimation = ObjectAnimator.ofFloat(vOverlay, View.ALPHA, 0, MAX_OVERLAY_LEVEL);
        } else {
            alphaAnimation = ObjectAnimator.ofFloat(vOverlay, View.ALPHA, MAX_OVERLAY_LEVEL, 0);
        }
        alphaAnimation.setDuration(FRAGMENT_TRANSACTION_TIME);
        alphaAnimation.start();
    }
}
