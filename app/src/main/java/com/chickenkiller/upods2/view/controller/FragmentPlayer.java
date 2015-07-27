package com.chickenkiller.upods2.view.controller;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;

/**
 * Created by alonzilberman on 7/27/15.
 */
public class FragmentPlayer extends Fragment {

    public static String TAG = "fragmentPlayer";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        return view;
    }
}
