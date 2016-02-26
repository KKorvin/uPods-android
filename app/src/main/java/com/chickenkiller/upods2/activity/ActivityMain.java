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
import com.chickenkiller.upods2.controllers.app.LoginMaster;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.controllers.app.SettingsManager;
import com.chickenkiller.upods2.controllers.internet.NetworkTasksService;
import com.chickenkiller.upods2.fragments.FragmentHelp;
import com.chickenkiller.upods2.fragments.FragmentMediaItemsGrid;
import com.chickenkiller.upods2.fragments.FragmentProfile;
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
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.enums.ContextMenuType;
import com.chickenkiller.upods2.utils.enums.MediaItemType;
import com.chickenkiller.upods2.utils.ui.UIHelper;
import com.chickenkiller.upods2.views.SlidingMenu;
import com.facebook.CallbackManager;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Arrays;

public class ActivityMain extends BasicActivity implements IOverlayable, IToolbarHolder, ISlidingMenuHolder, ILoginManager, Toolbar.OnMenuItemClickListener {

    private static final int MIN_NUMBER_FRAGMENTS_IN_STACK = 1;
    private static final float MAX_OVERLAY_LEVEL = 0.8f;
    private static final int FRAGMENT_TRANSACTION_TIME = 300;
    private static final int WELLCOME_SCREEN_TIME = 2000;

    public static boolean isFirstRun = true;

    /**
     * Type of last fragment shown by activity
     */
    public static int lastFragmentType = -1;

    /**
     * Number of last fragment shown by activity (for view pagers)
     */
    public static int lastChildFragmentNumber = -1;

    /**
     * Type of last fragment's child i.e Search (child of main featured fragment)
     */
    public static int lastChildFragmentType = -1;

    private Toolbar toolbar;
    private SlidingMenu slidingMenu;
    private View vOverlay;

    private int[] notificationsActions = {NetworkTasksService.NOTIFICATIONS_SHOW_PODCASTS_SUBSCRIBED};
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
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        MenuItem searchMenuItem = toolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        UIHelper.changeSearchViewTextColor(searchView, Color.WHITE);

        slidingMenu = new SlidingMenu(this, toolbar);

        int startedFrom = getIntent().getIntExtra(ActivityPlayer.ACTIVITY_STARTED_FROM, -1);

        if (isFirstRun && !Arrays.asList(notificationsActions).contains(startedFrom)) {
            toolbar.setVisibility(View.GONE);
            showFragment(R.id.fl_content, new FragmentWellcome(), FragmentWellcome.TAG);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (!Prefs.getBoolean(FragmentHelp.PREF_HELP_SHOWN, false)) {
                        showHelpFragment();
                    } else {
                        toolbar.setVisibility(View.VISIBLE);
                        showFragment(R.id.fl_content, getStartFragment(), FragmentMediaItemsGrid.TAG);
                    }
                }
            }, WELLCOME_SCREEN_TIME);
        } else {
            toolbar.setVisibility(View.VISIBLE);

            int startedFragmentNumber = ActivityMain.lastChildFragmentNumber;

            if (startedFrom == NetworkTasksService.NOTIFICATIONS_SHOW_PODCASTS_SUBSCRIBED) {
                startedFragmentNumber = 1;
                startedFrom = MediaItemType.PODCAST.ordinal();
            }

            FragmentMediaItemsGrid fragmentMediaItemsGrid = new FragmentMediaItemsGrid();
            fragmentMediaItemsGrid.setMediaItemType(startedFrom == MediaItemType.PODCAST.ordinal() ? MediaItemType.PODCAST : MediaItemType.RADIO);
            if (startedFragmentNumber >= 0) {
                fragmentMediaItemsGrid.setStartItemNumber(startedFragmentNumber);
            }
            showFragment(R.id.fl_content, fragmentMediaItemsGrid, FragmentMediaItemsGrid.TAG);
        }
        isFirstRun = false;
    }


    private void showHelpFragment() {
        FragmentHelp fragmentHelp = new FragmentHelp();
        fragmentHelp.setCloseClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setVisibility(View.VISIBLE);
                FragmentMediaItemsGrid fragmentMediaItemsGrid = new FragmentMediaItemsGrid();
                fragmentMediaItemsGrid.setMediaItemType(MediaItemType.RADIO);
                showFragment(R.id.fl_content, fragmentMediaItemsGrid, FragmentMediaItemsGrid.TAG);
            }
        });
        showFragment(R.id.fl_content, fragmentHelp, FragmentHelp.TAG);
    }

    private FragmentMediaItemsGrid getStartFragment() {
        FragmentMediaItemsGrid fragmentMediaItemsGrid = new FragmentMediaItemsGrid();
        String startScreen = SettingsManager.getInstace().getStringSettingValue(SettingsManager.JS_START_SCREEN);
        if (startScreen.equals("rs_subscribed")) {
            fragmentMediaItemsGrid.setMediaItemType(MediaItemType.RADIO);
            fragmentMediaItemsGrid.setStartItemNumber(1);
        } else if (startScreen.equals("rs_recent")) {
            fragmentMediaItemsGrid.setMediaItemType(MediaItemType.RADIO);
            fragmentMediaItemsGrid.setStartItemNumber(2);
        } else if (startScreen.equals("podcasts_featured")) {
            fragmentMediaItemsGrid.setMediaItemType(MediaItemType.PODCAST);
            fragmentMediaItemsGrid.setStartItemNumber(0);
        } else if (startScreen.equals("podcasts_favorites")) {
            fragmentMediaItemsGrid.setMediaItemType(MediaItemType.PODCAST);
            fragmentMediaItemsGrid.setStartItemNumber(1);
        } else if (startScreen.equals("podcasts_downloaded")) {
            fragmentMediaItemsGrid.setMediaItemType(MediaItemType.PODCAST);
            fragmentMediaItemsGrid.setStartItemNumber(2);
        } else {
            fragmentMediaItemsGrid.setMediaItemType(MediaItemType.RADIO);
            fragmentMediaItemsGrid.setStartItemNumber(0);
        }
        return fragmentMediaItemsGrid;
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
        } else if (contextMenuType == ContextMenuType.EPISODE_MIDDLE_SCREEN) {
            inflater.inflate(R.menu.menu_basic_sceleton, menu);
            menu.add(getString(R.string.marks_as_played));
            MediaItem mediaItem = ((MediaItem.MediaItemBucket) currentContextMenuData).mediaItem;
            Track track = ((MediaItem.MediaItemBucket) currentContextMenuData).track;
            if (ProfileManager.getInstance().isDownloaded((IPlayableMediaItem) mediaItem, track)) {
                menu.add(getString(R.string.delete));
            }
        } else if (contextMenuType == ContextMenuType.PROFILE) {
            inflater.inflate(R.menu.menu_logedin_profile, menu);
        } else {
            inflater.inflate(R.menu.menu_activity_main, menu);
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
        } else if (id == R.id.itemLogout) {
            LoginMaster.getInstance().logout();
            getSlidingMenu().updateHeader(true);
            if (onContextItemSelected != null) {
                onContextItemSelected.operationFinished();
            }
        } else if (id == R.id.itemSync) {
            ContextMenuHelper.syncWithCloud(this, onContextItemSelected);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (getFragmentManager().findFragmentByTag(getLatestFragmentTag()) instanceof FragmentProfile) {
            FragmentProfile fragmentProfile = (FragmentProfile) getFragmentManager().findFragmentByTag(getLatestFragmentTag());
            fragmentProfile.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public CallbackManager getFacebookCallbackManager() {
        return callbackManager;
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_podcast) {
            ContextMenuHelper.showAddMediaDialog(this, MediaItemType.PODCAST);
        } else if (id == R.id.add_radio) {
            ContextMenuHelper.showAddMediaDialog(this, MediaItemType.RADIO);
        }
        return true;
    }
}
