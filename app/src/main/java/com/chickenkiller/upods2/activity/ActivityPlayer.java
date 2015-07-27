package com.chickenkiller.upods2.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.view.controller.FragmentMainFeatured;
import com.chickenkiller.upods2.view.controller.FragmentPlayer;

public class ActivityPlayer extends Activity implements IFragmentsManager {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        showFragment(R.id.fl_window, new FragmentPlayer(), FragmentMainFeatured.TAG);
    }

    @Override
    public void showFragment(int id, Fragment fragment, String tag, IFragmentsManager.FragmentOpenType openType, IFragmentsManager.FragmentAnimationType animationType) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (animationType == IFragmentsManager.FragmentAnimationType.BOTTOM_TOP) {
            ft.setCustomAnimations(R.animator.animation_fragment_bototm_top, R.animator.animation_fragment_top_bototm,
                    R.animator.animation_fragment_bototm_top, R.animator.animation_fragment_top_bototm);
        }
        if (openType == IFragmentsManager.FragmentOpenType.OVERLAY) {
            ft.add(id, fragment, tag);
        } else {
            ft.replace(id, fragment, tag);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(tag);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1 || getLatestFragmentTag().equals(FragmentPlayer.TAG)) {
            getFragmentManager().popBackStack();
        } else {
            Intent myIntent = new Intent(this, ActivityMain.class);
            startActivity(myIntent);
            finish();
        }
    }

    @Override
    public void showFragment(int id, Fragment fragment, String tag) {
        showFragment(id, fragment, tag, IFragmentsManager.FragmentOpenType.REPLACE, IFragmentsManager.FragmentAnimationType.DEFAULT);
    }

    @Override
    public boolean hasFragment(String tag) {
        return getFragmentManager().findFragmentByTag(tag) != null;
    }

    @Override
    public String getLatestFragmentTag() {
        String tag = "";
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
            tag = backEntry.getName();
        }
        return tag;
    }

    @Override
    protected void onStop() {
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        super.onStop();
    }

}
