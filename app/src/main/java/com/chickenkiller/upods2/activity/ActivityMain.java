package com.chickenkiller.upods2.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.fragments.FragmentMediaItemsGrid;
import com.chickenkiller.upods2.fragments.FragmentSearch;
import com.chickenkiller.upods2.fragments.FragmentWellcome;
import com.chickenkiller.upods2.interfaces.ICustumziedBackPress;
import com.chickenkiller.upods2.interfaces.ILoginManager;
import com.chickenkiller.upods2.interfaces.IOverlayable;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.MediaItem;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.models.Track;
import com.chickenkiller.upods2.utils.ContextMenuHelper;
import com.chickenkiller.upods2.utils.DataHolder;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.enums.ContextMenuType;
import com.chickenkiller.upods2.utils.enums.MediaItemType;
import com.chickenkiller.upods2.utils.ui.UIHelper;
import com.chickenkiller.upods2.views.SlidingMenu;
import com.facebook.CallbackManager;

public class ActivityMain extends BasicActivity implements IOverlayable, IToolbarHolder, ISlidingMenuHolder, ILoginManager {

    private static final int MIN_NUMBER_FRAGMENTS_IN_STACK = 2;
    private static final float MAX_OVERLAY_LEVEL = 0.8f;
    private static final int FRAGMENT_TRANSACTION_TIME = 300;
    private static final int WELLCOME_SCREEN_TIME = 2000;
    private Toolbar toolbar;
    private SlidingMenu slidingMenu;
    private View vOverlay;

    public static boolean isFirstRun = true;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vOverlay = findViewById(R.id.vOverlay);

        //Social
        callbackManager = CallbackManager.Factory.create();

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.inflateMenu(R.menu.menu_activity_main);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        MenuItem searchMenuItem = toolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        UIHelper.changeSearchViewTextColor(searchView, Color.WHITE);

        slidingMenu = new SlidingMenu(this, toolbar);
        showFragment(R.id.fl_content, new FragmentWellcome(), FragmentWellcome.TAG);

        if (isFirstRun) {
            toolbar.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    toolbar.setVisibility(View.VISIBLE);
                    FragmentMediaItemsGrid fragmentMediaItemsGrid = new FragmentMediaItemsGrid();
                    fragmentMediaItemsGrid.setMediaItemType(MediaItemType.RADIO);
                    showFragment(R.id.fl_content, fragmentMediaItemsGrid, FragmentMediaItemsGrid.TAG);
                }
            }, WELLCOME_SCREEN_TIME);
        } else {
            toolbar.setVisibility(View.VISIBLE);

            int startedFrom = getIntent().getIntExtra(ActivityPlayer.ACTIVITY_STARTED_FROM, -1);
            int startedFragmentNumber = -1;

            //In order to return to fragment inside view pager which leaved activity
            startedFragmentNumber = DataHolder.getInstance().contains(FragmentMediaItemsGrid.LAST_ITEM_POSITION) ?
                    (int) DataHolder.getInstance().retrieve(FragmentMediaItemsGrid.LAST_ITEM_POSITION) : -1;

            FragmentMediaItemsGrid fragmentMediaItemsGrid = new FragmentMediaItemsGrid();
            fragmentMediaItemsGrid.setMediaItemType(startedFrom == MediaItemType.PODCAST.ordinal() ? MediaItemType.PODCAST : MediaItemType.RADIO);
            if (startedFragmentNumber >= 0) {
                fragmentMediaItemsGrid.setStartItemNumber(startedFragmentNumber);
            }
            showFragment(R.id.fl_content, fragmentMediaItemsGrid, FragmentMediaItemsGrid.TAG);
        }
        isFirstRun = false;
    }


    @Override
    public void onBackPressed() {
        if (getFragmentManager().findFragmentByTag(getLatestFragmentTag()) instanceof ICustumziedBackPress) {
            boolean performBackPress = ((ICustumziedBackPress) getFragmentManager().findFragmentByTag(getLatestFragmentTag())).onBackPressed();
            if (!performBackPress) {
                return;
            }
        }

        //TODO change
        slidingMenu.getAdapter().clearRowSelections();
        slidingMenu.getAdapter().notifyDataSetChanged();
        Logger.printInfo(LOG_TAG, "Number fragments in stack: " + String.valueOf(getFragmentManager().getBackStackEntryCount()));
        if (getFragmentManager().getBackStackEntryCount() > MIN_NUMBER_FRAGMENTS_IN_STACK) {
            if (getLatestFragmentTag().equals(FragmentSearch.TAG)) {
                toolbar.getMenu().findItem(R.id.action_search).collapseActionView();
                getFragmentManager().popBackStack();
            } else {
                if (isOverlayShown()) {
                    toggleOverlay();
                }
                getFragmentManager().popBackStack();
            }
        } else {
            finish();
        }

    }

    @Override
    public void toggleOverlay() {
        ObjectAnimator alphaAnimation;
        if (isOverlayShown()) {
            alphaAnimation = ObjectAnimator.ofFloat(vOverlay, View.ALPHA, MAX_OVERLAY_LEVEL, 0);
        } else {
            alphaAnimation = ObjectAnimator.ofFloat(vOverlay, View.ALPHA, 0, MAX_OVERLAY_LEVEL);
        }
        alphaAnimation.setDuration(FRAGMENT_TRANSACTION_TIME);
        alphaAnimation.start();
    }

    @Override
    public boolean isOverlayShown() {
        return vOverlay.getAlpha() != 0;
    }

    @Override
    public void setOverlayAlpha(int alphaPercent) {
        vOverlay.getBackground().setAlpha(alphaPercent);
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public SlidingMenu getSlidingMenu() {
        return slidingMenu;
    }

    @Override
    public void setSlidingMenuHeader(String itemName) {
        slidingMenu.getAdapter().setSelectedRow(itemName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Facebook analytics
        //AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Facebook analytics
        //AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (currentContextMenuData != null && currentContextMenuData instanceof IPlayableMediaItem) {
            Toast.makeText(this, ((IPlayableMediaItem) currentContextMenuData).getName(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        if (contextMenuType == ContextMenuType.PODCAST_MIDDLE_SCREEN) {
            inflater.inflate(R.menu.menu_basic_sceleton, menu);
            menu.add(getString(R.string.about_podcast));
            if (ProfileManager.getInstance().isDownloaded((Podcast) currentContextMenuData)) {
                menu.add(getString(R.string.open_on_disk));
                menu.add(getString(R.string.remove_all_episods));
            }
        } else if (contextMenuType == ContextMenuType.EPISOD_MIDDLE_SCREEN) {
            inflater.inflate(R.menu.menu_basic_sceleton, menu);
            menu.add(getString(R.string.marks_as_played));
            MediaItem mediaItem = ((MediaItem.MediaItemBucket) currentContextMenuData).mediaItem;
            Track track = ((MediaItem.MediaItemBucket) currentContextMenuData).track;
            if (ProfileManager.getInstance().isDownloaded((IPlayableMediaItem) mediaItem, track)) {
                menu.add(getString(R.string.delete));
            }
        } else {
            inflater.inflate(R.menu.menu_media_item_feature, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (currentContextMenuData != null && currentContextMenuData instanceof Podcast
                && item.getTitle().equals(getString(R.string.about_podcast))) {
            ContextMenuHelper.showAboutPodcastDialog((Podcast) currentContextMenuData, this);
        } else if (currentContextMenuData != null && currentContextMenuData instanceof Podcast
                && item.getTitle().equals(getString(R.string.open_on_disk))) {
            ContextMenuHelper.showPodcastInFolder((IPlayableMediaItem) currentContextMenuData, this);
        } else if (currentContextMenuData != null && currentContextMenuData instanceof Podcast
                && item.getTitle().equals(getString(R.string.remove_all_episods))) {
            ContextMenuHelper.removeAllDonwloadedEpisods(this, (Podcast) currentContextMenuData, onContextItemSelected);
        } else if (currentContextMenuData != null && currentContextMenuData instanceof MediaItem.MediaItemBucket
                && item.getTitle().equals(getString(R.string.delete))) {
            MediaItem mediaItem = ((MediaItem.MediaItemBucket) currentContextMenuData).mediaItem;
            Track track = ((MediaItem.MediaItemBucket) currentContextMenuData).track;
            ContextMenuHelper.removeDonwloadedTrack(this, track, (IPlayableMediaItem) mediaItem, onContextItemSelected);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public CallbackManager getFacebookCallbackManager() {
        return callbackManager;
    }
}
