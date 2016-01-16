package com.chickenkiller.upods2.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.player.UniversalPlayer;
import com.chickenkiller.upods2.fragments.FragmentMainFeatured;
import com.chickenkiller.upods2.fragments.FragmentPlayer;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.utils.DataHolder;

public class ActivityPlayer extends BasicActivity implements IToolbarHolder, Toolbar.OnMenuItemClickListener {

    public static final String MEDIA_ITEM_EXTRA = "mediaItem";
    public static final String ACTIVITY_STARTED_FROM = "startedFrom";
    public static final String ACTIVITY_STARTED_FROM_IN_DEPTH = "startedFromDepth"; //used if activity was started from depth fragment i.e search

    private IPlayableMediaItem currentMediaItem;
    private Toolbar toolbar;
    private ActionMenuItemView itemFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        toolbar = (Toolbar) findViewById(R.id.toolbar_player);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.inflateMenu(R.menu.menu_activity_player);
        toolbar.setOnMenuItemClickListener(this);
        itemFavorites = (ActionMenuItemView) toolbar.findViewById(R.id.action_favorites_player);

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
                myIntent.putExtra(ACTIVITY_STARTED_FROM_IN_DEPTH, getIntent().getIntExtra(ACTIVITY_STARTED_FROM_IN_DEPTH, -1));
            }
            startActivity(myIntent);
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
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


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        IPlayableMediaItem mediaItem = UniversalPlayer.getInstance().getPlayingMediaItem();
        if (ProfileManager.getInstance().isSubscribedToMediaItem(mediaItem)) {
            itemFavorites.setIcon(getResources().getDrawable(R.drawable.ic_heart_white_24dp));
            ProfileManager.getInstance().removeSubscribedMediaItem(mediaItem);
            Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
        } else {
            itemFavorites.setIcon(getResources().getDrawable(R.drawable.ic_heart_black_24dp));
            ProfileManager.getInstance().addSubscribedMediaItem(mediaItem);
            Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
