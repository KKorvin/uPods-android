package com.chickenkiller.upods2.interfaces;

import org.json.JSONObject;

/**
 * Created by alonzilberman on 7/11/15.
 * Handles http responses when response is json
 */
public interface INetworkUIupdater {

    void updateUISuccess(JSONObject jResponse);

    void updateUIFailed();
}
