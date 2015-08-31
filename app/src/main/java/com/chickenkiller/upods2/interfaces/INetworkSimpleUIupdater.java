package com.chickenkiller.upods2.interfaces;

/**
 * Created by alonzilberman on 7/11/15.
 * Handles http responses when response is simple string
 */
public interface INetworkSimpleUIupdater{

    void updateUISuccess(String response);

    void updateUIFailed();

}
