package com.chickenkiller.upods2.interfaces;

import org.json.JSONObject;

/**
 * Created by alonzilberman on 7/11/15.
 */
public interface INetworkUIupdater {

    void updateUISuccess(JSONObject jResponse);

    void updateUIFailed();
}
