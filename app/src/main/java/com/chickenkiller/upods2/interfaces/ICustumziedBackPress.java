package com.chickenkiller.upods2.interfaces;

/**
 * Imliment it in Fragment to override back press logic
 */
public interface ICustumziedBackPress {

    /**
     * @return true if you want to perform super  onBackPressed after Fragment onBackPressed or false otherwise
     */
    boolean onBackPressed();
}
