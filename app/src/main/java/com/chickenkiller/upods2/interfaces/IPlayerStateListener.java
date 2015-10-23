package com.chickenkiller.upods2.interfaces;

import com.chickenkiller.upods2.controllers.player.UniversalPlayer;

/**
 * Created by alonzilberman on 7/31/15.
*/
public interface IPlayerStateListener {

    void onStateChanged(UniversalPlayer.State state);
}

