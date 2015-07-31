package com.chickenkiller.upods2.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.UniversalPlayer;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.RadioItem;
import com.chickenkiller.upods2.view.controller.FragmentMainFeatured;
import com.chickenkiller.upods2.view.controller.FragmentPlayer;

public class ActivityPlayer extends FragmentsActivity {

    public static final String RADIO_ITEM_EXTRA = "radioItem";
    private MediaItem currentMediaItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (getIntent().hasExtra(RADIO_ITEM_EXTRA)) {
            currentMediaItem = (RadioItem) getIntent().getExtras().get(RADIO_ITEM_EXTRA);
        } else if (savedInstanceState.getParcelable(RADIO_ITEM_EXTRA) != null) {
            currentMediaItem = (RadioItem) savedInstanceState.getSerializable(RADIO_ITEM_EXTRA);
        } else if (UniversalPlayer.getInstance().isPlaying()) {
            currentMediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
        }
        if (currentMediaItem == null) {
            throw new RuntimeException("Can't run activity player, MediaItem for play not set.");
        }
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            FragmentPlayer fragmentPlayer = new FragmentPlayer();
            fragmentPlayer.setRadioItem((RadioItem) currentMediaItem);
            showFragment(R.id.fl_window, fragmentPlayer, FragmentMainFeatured.TAG);
        }
    }


    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1 || getLatestFragmentTag().equals(FragmentPlayer.TAG)) {
            getFragmentManager().popBackStack();
        } else {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Intent myIntent = new Intent(this, ActivityMain.class);
            startActivity(myIntent);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
