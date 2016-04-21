package com.chickenkiller.upods2.interfaces;


import android.app.DialogFragment;
import android.app.Fragment;

/**
 * Created by alonzilberman on 7/8/15.
 */
public interface IFragmentsManager {

    enum FragmentOpenType {REPLACE, OVERLAY}

    enum FragmentAnimationType {DEFAULT, BOTTOM_TOP}


    void showFragment(int id, Fragment fragment, String tag, FragmentOpenType openType, FragmentAnimationType animationType);

    void showFragment(int id, Fragment fragment, String tag);

    void hideFragment(Fragment fragment);

    void showDialogFragment(DialogFragment dialogFragment);

    boolean hasFragment(String tag);

    String getLatestFragmentTag();
}
