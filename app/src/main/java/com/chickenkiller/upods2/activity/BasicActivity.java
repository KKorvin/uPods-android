package com.chickenkiller.upods2.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.interfaces.IContextMenuManager;
import com.chickenkiller.upods2.interfaces.IControlStackHistory;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IOverlayable;
import com.chickenkiller.upods2.utils.enums.ContextMenuType;

import java.util.Calendar;

/**
 * Created by alonzilberman on 7/28/15.
 * Extend this activity to get basic logic for context menu and fragments working
 */
public class BasicActivity extends Activity implements IFragmentsManager, IContextMenuManager {

    protected final static String LOG_TAG = "BasicActivity";
    private final static String DIALOG_TAG_START = "fr_dialog_";

    //For context menus
    protected Object currentContextMenuData;
    protected ContextMenuType contextMenuType;
    protected IOperationFinishCallback onContextItemSelected;
    private boolean isContextItemSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void showFragment(int id, Fragment fragment, String tag, IFragmentsManager.FragmentOpenType openType, IFragmentsManager.FragmentAnimationType animationType) {
        android.app.FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (animationType == IFragmentsManager.FragmentAnimationType.BOTTOM_TOP) {
            ft.setCustomAnimations(R.animator.animation_fragment_bototm_top, R.animator.animation_fragment_top_bototm,
                    R.animator.animation_fragment_bototm_top, R.animator.animation_fragment_top_bototm);
        }
        if (openType == IFragmentsManager.FragmentOpenType.OVERLAY) {
            ft.add(id, fragment, tag);
            if (this instanceof IOverlayable) {
                ((IOverlayable) this).toggleOverlay();
            }
        } else {
            ft.replace(id, fragment, tag);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (fragment instanceof IControlStackHistory) {//Decide ether fragment should be added to stack history
            if (((IControlStackHistory) fragment).shouldBeAddedToStack()) {
                ft.addToBackStack(tag);
            }
        } else {
            ft.addToBackStack(tag);
        }

        ft.commitAllowingStateLoss();
    }


    @Override
    public void showFragment(int id, Fragment fragment, String tag) {
        showFragment(id, fragment, tag, IFragmentsManager.FragmentOpenType.REPLACE, IFragmentsManager.FragmentAnimationType.DEFAULT);
    }

    @Override
    public void hideFragment(Fragment fragment) {
        android.app.FragmentManager fm = getFragmentManager();
        fm.beginTransaction().remove(fragment).commit();
    }

    @Override
    public void showDialogFragment(DialogFragment dialogFragment) {
        long time = Calendar.getInstance().get(Calendar.MILLISECOND);
        String tag = DIALOG_TAG_START + String.valueOf(time);
        dialogFragment.show(getFragmentManager(), tag);
    }

    @Override
    public boolean hasFragment(String tag) {
        return getFragmentManager().findFragmentByTag(tag) != null;
    }

    @Override
    public String getLatestFragmentTag() {
        String tag = "";
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
            tag = backEntry.getName();
        }
        return tag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void openContextMenu(View view, ContextMenuType type, Object dataToPass, IOperationFinishCallback actionFinished) {
        currentContextMenuData = dataToPass;
        contextMenuType = type;
        onContextItemSelected = actionFinished;
        registerForContextMenu(view);
        openContextMenu(view);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        if (!isContextItemSelected) {
            currentContextMenuData = null;
            contextMenuType = null;
            onContextItemSelected = null;
        }
        isContextItemSelected = false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        isContextItemSelected = true;
        currentContextMenuData = null;
        contextMenuType = null;
        onContextItemSelected = null;
        return super.onContextItemSelected(item);
    }

}
