package com.chickenkiller.upods2.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentSettings extends PreferenceFragment {

    public static String TAG = "preference";
    private ListPreference mListPreference;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((ISlidingMenuHolder) getActivity()).setSlidingMenuHeader(getString(R.string.action_settings));

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onPause() {
        SettingsManager.getInstace().initSettingsFromPreferences();
        super.onPause();
    }

}
