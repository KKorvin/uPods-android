package com.chickenkiller.upods2.controllers.app;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.chickenkiller.upods2.utils.Logger;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alonzilberman on 11/29/15.
 */
public class LoginMaster {

    private static final String LOG_TAG = "LoginMaster";
    private static final String IDENTITY_POOL = "us-east-1:1bef9ad3-34bd-42d2-8e71-50c648aabf32";
    private static LoginMaster loginMaster;

    private CognitoCachingCredentialsProvider credentialsProvider;
    private boolean isLogedinWithFacebook;

    private LoginMaster() {
        this.isLogedinWithFacebook = false;
    }


    public static LoginMaster getInstance() {
        if (loginMaster == null) {
            loginMaster = new LoginMaster();
        }
        return loginMaster;
    }

    public void init() {
        credentialsProvider = new CognitoCachingCredentialsProvider(
                UpodsApplication.getContext(),
                IDENTITY_POOL, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        isLogedinWithFacebook = AccessToken.getCurrentAccessToken() != null;
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                isLogedinWithFacebook = (AccessToken.getCurrentAccessToken() != null);
                setFbAccountToCognito();
                Logger.printInfo(LOG_TAG, "Is loged in with facebook: " + String.valueOf(isLogedinWithFacebook));
            }
        };
        accessTokenTracker.startTracking();
        setFbAccountToCognito();
        Logger.printInfo(LOG_TAG, "Is loged in with facebook: " + String.valueOf(isLogedinWithFacebook));
    }

    private void setFbAccountToCognito() {
        if (isLogedinWithFacebook) {
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
            credentialsProvider.setLogins(logins);
        }
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public boolean isLogedIn() {
        return isLogedinWithFacebook;
    }

    public void logout() {
        if (isLogedinWithFacebook) {
            LoginManager.getInstance().logOut();
            Logger.printInfo(LOG_TAG, "Loged out from facebook");
        }
    }
}
