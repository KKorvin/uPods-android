package com.chickenkiller.upods2.view.controller;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.chickenkiller.upods2.R;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentSettings extends PreferenceFragment {

    private ListPreference mListPreference;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }


}
