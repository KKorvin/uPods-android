package com.chickenkiller.upods2.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.view.controller.FragmentMainFeatured;
import com.chickenkiller.upods2.view.controller.FragmentPlayer;

public class ActivityPlayer extends FragmentsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        showFragment(R.id.fl_window, new FragmentPlayer(), FragmentMainFeatured.TAG);
    }


    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1 || getLatestFragmentTag().equals(FragmentPlayer.TAG)) {
            getFragmentManager().popBackStack();
        } else {
            Intent myIntent = new Intent(this, ActivityMain.class);
            startActivity(myIntent);
            finish();
        }
    }


    @Override
    protected void onStop() {
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        super.onStop();
    }

}
