package com.chickenkiller.upods2.interfaces;

import com.chickenkiller.upods2.views.SlidingMenu;

/**
 * Created by Alon Zilberman on 7/10/15.
 */
public interface ISlidingMenuHolder{

    SlidingMenu getSlidingMenu();

    void setSlidingMenuHeader(String itemName);
}
