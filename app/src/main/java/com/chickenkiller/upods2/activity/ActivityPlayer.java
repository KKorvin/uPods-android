package com.chickenkiller.upods2.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.view.controller.FragmentMainFeatured;
import com.chickenkiller.upods2.view.controller.FragmentPlayer;

public class ActivityPlayer extends FragmentsActivity {

    public static final String RADIO_ITEM_EXTRA = "radioItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        FragmentPlayer fragmentPlayer = new FragmentPlayer();
        if (getIntent().hasExtra(RADIO_ITEM_EXTRA)) {
            fragmentPlayer.setRadioItem((RadioItem) getIntent().getExtras().get(RADIO_ITEM_EXTRA));
        }
        showFragment(R.id.fl_window, fragmentPlayer, FragmentMainFeatured.TAG);
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
