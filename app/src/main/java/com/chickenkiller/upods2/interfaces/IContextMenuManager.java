package com.chickenkiller.upods2.interfaces;

import android.view.View;

import com.chickenkiller.upods2.utils.ContextMenuType;

/**
 * Implement it on activity for costumizing work with different sliding menus
 */
public interface IContextMenuManager {

    /**
     * @param view       - which shows context menu
     * @param type       - type of context menu to show
     * @param dataToPass - data which will be used for actions inside menu
     */
    void openContextMenu(View view, ContextMenuType type, Object dataToPass, OnActionFinished contextItemClicked);
}
