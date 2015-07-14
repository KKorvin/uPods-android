package com.chickenkiller.upods2.interfaces;


import android.app.Fragment;

/**
 * Created by alonzilberman on 7/8/15.
 */
public interface IFragmentsManager {

    public static enum FragmentOpenType {REPLACE, OVERLAY}

    public static enum FragmentAnimationType {DEFAULT, BOTTOM_TOP}


    void showFragment(int id, Fragment fragment, String tag, FragmentOpenType openType, FragmentAnimationType animationType);

    void showFragment(int id, Fragment fragment, String tag);

    int getCurrentMainFragmentId();
}
