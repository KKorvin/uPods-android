package com.chickenkiller.upods2.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.controllers.app.UpodsApplication;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentSettings extends PreferenceFragment {

    public static String TAG = "preference";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.action_settings));

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onPause() {
        SettingsManager.getInstace().initSettingsFromPreferences();
        UpodsApplication.setAlarmManagerTasks();
        super.onPause();
    }

}
