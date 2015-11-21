package com.chickenkiller.upods2.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.fragments.FragmentMainFeatured;
import com.chickenkiller.upods2.fragments.FragmentPlayer;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.utils.DataHolder;

public class ActivityPlayer extends BasicActivity implements IToolbarHolder {

    public static final String MEDIA_ITEM_EXTRA = "mediaItem";
    public static final String ACTIVITY_STARTED_FROM = "startedFrom";

    private IPlayableMediaItem currentMediaItem;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        toolbar = (Toolbar) findViewById(R.id.toolbar_player);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.inflateMenu(R.menu.menu_activity_player);

        if (DataHolder.getInstance().contains(MEDIA_ITEM_EXTRA)) {
            currentMediaItem = (IPlayableMediaItem) DataHolder.getInstance().retrieve(MEDIA_ITEM_EXTRA);
        } else if (getIntent().hasExtra(MEDIA_ITEM_EXTRA)) {
            currentMediaItem = (IPlayableMediaItem) getIntent().getExtras().get(MEDIA_ITEM_EXTRA);
        } else if (savedInstanceState != null && savedInstanceState.getParcelable(MEDIA_ITEM_EXTRA) != null) {
            currentMediaItem = (IPlayableMediaItem) savedInstanceState.getSerializable(MEDIA_ITEM_EXTRA);
        } else if (UniversalPlayer.getInstance().isPlaying()) {
            currentMediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
        } else if (currentMediaItem == null && UniversalPlayer.getInstance().getPlayingMediaItem() != null) {
            currentMediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
        }
        if (currentMediaItem == null) {
            throw new RuntimeException("Can't run activity player, MediaItem for play not set.");
        }
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            FragmentPlayer fragmentPlayer = new FragmentPlayer();
            fragmentPlayer.setPlayableItem(currentMediaItem);
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
            if (getIntent().hasExtra(ACTIVITY_STARTED_FROM)) {
                myIntent.putExtra(ACTIVITY_STARTED_FROM, getIntent().getIntExtra(ACTIVITY_STARTED_FROM, -1));
            }
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

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }
}
