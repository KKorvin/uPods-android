package com.chickenkiller.upods2.interfaces;

/**
 * Created by alonzilberman on 7/21/15.
 */
public interface IOverlayable {
    void toggleOverlay();
    boolean isOverlayShown();
    void setOverlayAlpha(int alphaPercent);
}
