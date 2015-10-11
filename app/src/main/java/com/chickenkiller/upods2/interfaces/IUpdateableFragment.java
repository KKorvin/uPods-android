package com.chickenkiller.upods2.interfaces;

/**
 * Implement it when you need to update fragment content from outside i.e from viewpager adaper or if you need let know to fragment that it must upgrade content
 */
public interface IUpdateableFragment {

    void update();
}
