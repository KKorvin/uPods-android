package com.chickenkiller.upods2.controllers.app;

import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.utils.Logger;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.login.LoginManager;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterSession;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Created by alonzilberman on 11/29/15.
 */
public class LoginMaster {

    private static final String LOG_TAG = "LoginMaster";
    private static final String IDENTITY_POOL_ID = "us-east-1:1bef9ad3-34bd-42d2-8e71-50c648aabf32";
    private static final String TWITTER_CONSUMER_KEY = "wr8t6lPMxtC09uMpIEayM5FBC";
    private static final String TWITTER_CONSUMER_SECRET = "dtnTy4RQfnowu60XGHToj830j4AYsKxDA82PWZBijgSdk0gnlk";

    private static LoginMaster loginMaster;

    private CognitoCachingCredentialsProvider credentialsProvider;
    private VKAuthenticationProvider vkAuthenticationProvider;

    private boolean isLogedinWithFacebook;
    private boolean isLogedinWithTwitter;

    private LoginMaster() {
        this.isLogedinWithFacebook = false;
        this.isLogedinWithTwitter = false;
    }


    public static LoginMaster getInstance() {
        if (loginMaster == null) {
            loginMaster = new LoginMaster();
        }
        return loginMaster;
    }

    public void init() {
        //First init VKSdk because it is used by vkAuthenticationProvider
        VKSdk.initialize(UpodsApplication.getContext());

        vkAuthenticationProvider = new VKAuthenticationProvider(
                null,
                IDENTITY_POOL_ID,
                Regions.US_EAST_1);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                UpodsApplication.getContext(),
                vkAuthenticationProvider,
                Regions.US_EAST_1
        );

        initFacebook();
        initTwitter();
        initVkontakte();
    }

    private void initFacebook() {
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
    }

    private void initTwitter() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
        Fabric.with(UpodsApplication.getContext(), new Twitter(authConfig));
    }

    private void initVkontakte() {
        VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
            @Override
            public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
                if (newToken == null) {
                    setVkToCognito(newToken, null);
                }
            }
        };
        vkAccessTokenTracker.startTracking();
    }

    private void setFbAccountToCognito() {
        if (isLogedinWithFacebook) {
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
            credentialsProvider.setLogins(logins);
        }
    }

    public void setTwitterToCognito(TwitterSession session) {
        TwitterAuthToken authToken = session.getAuthToken();
        String value = authToken.token + ";" + authToken.secret;
        Map<String, String> logins = new HashMap<String, String>();
        logins.put("api.twitter.com", value);
        // Note: This overrides any existing logins
        credentialsProvider.setLogins(logins);
    }

    public void setVkToCognito(final VKAccessToken token, final IOperationFinishCallback iLoginFinished) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> loginsMap = new HashMap<String, String>();
                loginsMap.put(vkAuthenticationProvider.getProviderName(), token.accessToken);
                credentialsProvider.setLogins(loginsMap);
                credentialsProvider.refresh();
                if (iLoginFinished != null) {
                    iLoginFinished.operationFinished();
                }
            }
        });
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public void setIsLogedinWithTwitter(boolean isLogedinWithTwitter) {
        this.isLogedinWithTwitter = isLogedinWithTwitter;
    }

    public boolean isLogedIn() {
        return isLogedinWithFacebook || isLogedinWithTwitter || VKSdk.isLoggedIn();
    }

    public void logout() {
        if (isLogedinWithFacebook) {
            LoginManager.getInstance().logOut();
            Logger.printInfo(LOG_TAG, "Loged out from facebook");
        } else if (isLogedinWithTwitter) {
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        } else if (VKSdk.isLoggedIn()) {
            //VKSdk.logout();
        }
    }
}
