package com.chickenkiller.upods2.activity;

import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.ProfileManager;
import com.chickenkiller.upods2.interfaces.IContextMenuManager;
import com.chickenkiller.upods2.interfaces.ICustumziedBackPress;
import com.chickenkiller.upods2.interfaces.IOverlayable;
import com.chickenkiller.upods2.interfaces.IPlayableMediaItem;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.interfaces.OnActionFinished;
import com.chickenkiller.upods2.models.Podcast;
import com.chickenkiller.upods2.utils.ContextMenuType;
import com.chickenkiller.upods2.utils.GlobalUtils;
import com.chickenkiller.upods2.utils.UIHelper;
import com.chickenkiller.upods2.view.controller.DialogFragmentConfarmation;
import com.chickenkiller.upods2.view.controller.DialogFragmentMessage;
import com.chickenkiller.upods2.view.controller.FragmentMainFeatured;
import com.chickenkiller.upods2.view.controller.FragmentSearch;
import com.chickenkiller.upods2.view.controller.FragmentWellcome;
import com.chickenkiller.upods2.view.controller.SlidingMenu;

import java.io.File;

public class ActivityMain extends FragmentsActivity implements IOverlayable, IToolbarHolder, ISlidingMenuHolder, IContextMenuManager {


    private static final float MAX_OVERLAY_LEVEL = 0.8f;
    private static final int FRAGMENT_TRANSACTION_TIME = 300;
    private static final int WELLCOME_SCREEN_TIME = 2000;
    private static boolean isFirstRun = true;
    private Toolbar toolbar;
    private SlidingMenu slidingMenu;
    private View vOverlay;

    private Object currentContextMenuData;
    private ContextMenuType contextMenuType;
    private OnActionFinished contextItemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vOverlay = findViewById(R.id.vOverlay);

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
                    showFragment(R.id.fl_content, new FragmentMainFeatured(), FragmentMainFeatured.TAG);
                }
            }, WELLCOME_SCREEN_TIME);
        } else {
            toolbar.setVisibility(View.VISIBLE);
            showFragment(R.id.fl_content, new FragmentMainFeatured(), FragmentMainFeatured.TAG);
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

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            if (getLatestFragmentTag().equals(FragmentMainFeatured.TAG)) {
                finish();
            } else if (getLatestFragmentTag().equals(FragmentSearch.TAG)) {
                toolbar.getMenu().findItem(R.id.action_search).collapseActionView();
                getFragmentManager().popBackStack();
            } else {
                if (isOverlayShown()) {
                    toggleOverlay();
                }
                getFragmentManager().popBackStack();
            }
        } else {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            super.onBackPressed();
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void openContextMenu(View view, ContextMenuType type, Object dataToPass, OnActionFinished actionFinished) {
        currentContextMenuData = dataToPass;
        contextMenuType = type;
        contextItemSelected = actionFinished;
        registerForContextMenu(view);
        openContextMenu(view);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
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
        if (contextMenuType == ContextMenuType.PODCAST_MIDDLE_SCREEN) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_podcast_middle_screen, menu);
            if (ProfileManager.getInstance().isDownloaded((Podcast) currentContextMenuData)) {
                menu.add(getString(R.string.open_on_disk));
                menu.add(getString(R.string.remove_all_episods));
            }
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_media_item_feature, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (currentContextMenuData != null && currentContextMenuData instanceof Podcast && id == R.id.about_podcast) {
            DialogFragmentMessage dialogFragmentMessage = new DialogFragmentMessage();
            dialogFragmentMessage.setTitle(((Podcast) currentContextMenuData).getName());
            dialogFragmentMessage.setMessage(((Podcast) currentContextMenuData).getDescription());
            showDialogFragment(dialogFragmentMessage);
        } else if (currentContextMenuData != null && currentContextMenuData instanceof Podcast
                && item.getTitle().equals(getString(R.string.open_on_disk))) {
            String path = ProfileManager.getInstance().getDownloadedMediaItemPath((IPlayableMediaItem) currentContextMenuData);
            Uri selectedUri = Uri.parse(path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(selectedUri, "*/*");
            startActivity(intent);
        } else if (currentContextMenuData != null && currentContextMenuData instanceof Podcast
                && item.getTitle().equals(getString(R.string.remove_all_episods))) {
            DialogFragmentConfarmation dialogFragmentConfarmation = new DialogFragmentConfarmation();
            dialogFragmentConfarmation.setTitle(((Podcast) currentContextMenuData).getName());
            dialogFragmentConfarmation.setMessage(getString(R.string.remove_podcast_conformation));
            dialogFragmentConfarmation.setPositiveClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = ProfileManager.getInstance().getDownloadedMediaItemPath((IPlayableMediaItem) currentContextMenuData);
                    GlobalUtils.deleteDirectory(new File(path));
                    ProfileManager.getInstance().removeDownloadedMediaItem((IPlayableMediaItem) currentContextMenuData);
                    contextItemSelected.onActionFinished();
                }
            });
            showDialogFragment(dialogFragmentConfarmation);
        }
        return super.onContextItemSelected(item);
    }
}
