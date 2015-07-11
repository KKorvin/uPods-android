package com.chickenkiller.upods2.interfaces;

import com.squareup.okhttp.Response;

/**
 * Created by alonzilberman on 7/11/15.
 */
public interface INetworkUIupdater {

    void updateUISuccess(Response response);

    void updateUIFailed();
}
