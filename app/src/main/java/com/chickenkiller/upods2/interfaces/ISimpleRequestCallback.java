package com.chickenkiller.upods2.interfaces;

/**
 * Created by alonzilberman on 7/11/15.
 * Handles http responses when response is simple string
 */
public interface ISimpleRequestCallback {

    void onRequestSuccessed(String response);

    void onRequestFailed();

}
