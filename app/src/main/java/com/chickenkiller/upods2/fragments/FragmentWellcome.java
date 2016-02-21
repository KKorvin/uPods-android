package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IControlStackHistory;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentWellcome extends Fragment implements IControlStackHistory {

    public static final String TAG = "fragment_wellcome";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wellcome, container, false);
        return view;
    }

    @Override
    public boolean shouldBeAddedToStack() {
        return false;
    }
}
