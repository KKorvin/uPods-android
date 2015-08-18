package com.chickenkiller.upods2.interfaces;

import com.chickenkiller.upods2.view.controller.SlidingMenu;

/**
 * Created by alonzilberman on 7/10/15.
 */
public interface ISlidingMenuHolder{

    SlidingMenu getSlidingMenu();

    void setSlidingMenuHeader(String itemName);
}
