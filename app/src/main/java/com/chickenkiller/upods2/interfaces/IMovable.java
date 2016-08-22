package com.chickenkiller.upods2.interfaces;


import android.view.MotionEvent;

/**
 * Created by Alon Zilberman on 7/8/15.
 */
public interface IMovable {

    void onMove(MotionEvent event, boolean applyMovement);
}
