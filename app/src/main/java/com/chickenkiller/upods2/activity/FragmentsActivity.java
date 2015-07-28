package com.chickenkiller.upods2.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IOverlayable;

/**
 * Created by alonzilberman on 7/28/15.
 */
public class FragmentsActivity extends Activity implements IFragmentsManager {

    @Override
    public void showFragment(int id, Fragment fragment, String tag, IFragmentsManager.FragmentOpenType openType, IFragmentsManager.FragmentAnimationType animationType) {
        android.app.FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (animationType == IFragmentsManager.FragmentAnimationType.BOTTOM_TOP) {
            ft.setCustomAnimations(R.animator.animation_fragment_bototm_top, R.animator.animation_fragment_top_bototm,
                    R.animator.animation_fragment_bototm_top, R.animator.animation_fragment_top_bototm);
        }
        if (openType == IFragmentsManager.FragmentOpenType.OVERLAY) {
            ft.add(id, fragment, tag);
            if (this instanceof IOverlayable) {
                ((IOverlayable) this).toggleOverlay();
            }
        } else {
            ft.replace(id, fragment, tag);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(tag);
        ft.commit();
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
            android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
            tag = backEntry.getName();
        }
        return tag;
    }
}
