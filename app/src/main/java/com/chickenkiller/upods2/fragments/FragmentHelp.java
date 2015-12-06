package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.HelpPagesAdapter;
import com.chickenkiller.upods2.views.CircleIndicator;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentHelp extends Fragment {

    public static final String TAG = "fragment_help";
    public static final String PREF_HELP_SHOWN = "help_shown";

    private HelpPagesAdapter helpPagesAdapter;
    private ViewPager vpHelp;
    private CircleIndicator indicatorHelp;
    private View.OnClickListener closeClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        vpHelp = (ViewPager) view.findViewById(R.id.vpHelp);
        indicatorHelp = (CircleIndicator) view.findViewById(R.id.indicatorHelp);
        helpPagesAdapter = new HelpPagesAdapter(getChildFragmentManager());
        helpPagesAdapter.setCloseClickListener(closeClickListener);
        vpHelp.setAdapter(helpPagesAdapter);
        indicatorHelp.setViewPager(vpHelp);
        Prefs.putBoolean(PREF_HELP_SHOWN, true);
        return view;
    }

    public void setCloseClickListener(View.OnClickListener closeClickListener) {
        this.closeClickListener = closeClickListener;
    }
}
