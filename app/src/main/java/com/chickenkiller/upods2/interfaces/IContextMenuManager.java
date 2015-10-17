package com.chickenkiller.upods2.interfaces;

import android.view.View;

/**
 * Implement it on activity for costumizing work with different sliding menus
 */
public interface IContextMenuManager {

    /**
     * @param view       - which shows context menu
     * @param dataToPass - data which will be used for actions inside menu
     */
    void openContextMenu(View view, Object dataToPass);
}
