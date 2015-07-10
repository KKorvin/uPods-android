package com.chickenkiller.upods2.interfaces;

import android.app.Fragment;

/**
 * Created by alonzilberman on 7/8/15.
 */
public interface IFragmentsManager {

    void showFragment(int id, Fragment fragment);

    int getCurrentMainFragmentId();
}
