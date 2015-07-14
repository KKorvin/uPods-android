package com.chickenkiller.upods2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.view.controller.FragmentMainFeatured;
import com.chickenkiller.upods2.view.controller.SlidingMenu;

public class ActivityMain extends Activity implements IFragmentsManager {


    private Toolbar toolbar;
    private SlidingMenu slidingMenu;
    private int currentMainFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.inflateMenu(R.menu.menu_activity_main);
        slidingMenu = new SlidingMenu(this, toolbar);
        showFragment(R.id.ln_content, new FragmentMainFeatured(), FragmentMainFeatured.TAG);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    @Override
    public void showFragment(int id, Fragment fragment, String tag, FragmentOpenType openType, FragmentAnimationType animationType) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (openType == FragmentOpenType.OVERLAY) {
            ft.add(id, fragment, tag);
        } else {
            ft.replace(id, fragment);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
        currentMainFragmentId = id;
    }

    @Override
    public void showFragment(int id, Fragment fragment, String tag) {
        showFragment(id, fragment, tag, FragmentOpenType.REPLACE, FragmentAnimationType.DEFAULT);
    }

    @Override
    public int getCurrentMainFragmentId() {
        return currentMainFragmentId;
    }
}
