package com.chickenkiller.upods2.view.controller;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chickenkiller.upods2.R;

/**
 * Created by alonzilberman on 7/8/15.
 */
public class FragmentMediaDetails extends PreferenceFragment {
    public static String TAG = "media_details";
    private LinearLayout lnDetailedTransparentHeader;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_media_details, container, false);
        lnDetailedTransparentHeader = (LinearLayout) view.findViewById(R.id.lnDetailedTransparentHeader);
        return view;
    }


}
