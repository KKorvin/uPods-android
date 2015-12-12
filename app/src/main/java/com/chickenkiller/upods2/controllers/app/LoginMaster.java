package com.chickenkiller.upods2.controllers.app;

import android.os.AsyncTask;
import android.os.Bundle;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.chickenkiller.upods2.controllers.internet.VKAuthenticationProvider;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.models.UserProfile;
import com.chickenkiller.upods2.utils.Logger;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.pixplicity.easyprefs.library.Prefs;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

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

    private UserProfile userProfile;

    private LoginMaster() {
        this.isLogedinWithFacebook = false;
        this.isLogedinWithTwitter = false;
        this.userProfile = null;
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
        Prefs.remove(VKAuthenticationProvider.PREF_IDENTITY_ID);
        TwitterAuthToken authToken = session.getAuthToken();
        String value = authToken.token + ";" + authToken.secret;
        Map<String, String> logins = new HashMap<String, String>();
        logins.put("api.twitter.com", value);
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
            VKSdk.logout();
        }
    }

    public void initUserProfile(final IOperationFinishWithDataCallback profileFetched, boolean isForceUpdate) {
        if (userProfile != null && !isForceUpdate) {
            profileFetched.operationFinished(userProfile);
        } else if (!isLogedIn()) {
            userProfile = new UserProfile();
            profileFetched.operationFinished(userProfile);
        } else {
            if (isLogedinWithFacebook) {
                fetchFacebookUserData(profileFetched);
            } else if (isLogedinWithTwitter) {
                fetchTwitterUserData(profileFetched);
            } else if (VKSdk.isLoggedIn()) {
                fetchVkUserData(profileFetched);
            }
        }
    }

    private void fetchVkUserData(final IOperationFinishWithDataCallback profileFetched) {
        VKParameters parameters = new VKParameters();
        parameters.put("fields", "photo_200");
        VKRequest request = VKApi.users().get(parameters);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                userProfile = new UserProfile();
                try {
                    JSONObject jsonUser = response.json.getJSONArray("response").optJSONObject(0);
                    StringBuilder userName = new StringBuilder();
                    if (jsonUser.has("first_name")) {
                        userName.append(jsonUser.getString("first_name"));
                    }
                    if (jsonUser.has("last_name")) {
                        userName.append(" ");
                        userName.append(jsonUser.getString("last_name"));
                    }
                    userProfile.setName(userName.toString());
                    userProfile.setProfileImageUrl(jsonUser.getString("photo_200"));
                    profileFetched.operationFinished(userProfile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            }
        });
    }

    private void fetchTwitterUserData(final IOperationFinishWithDataCallback profileFetched) {
        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();
        Twitter.getApiClient(session).getAccountService()
                .verifyCredentials(true, false, new Callback<User>() {
                    @Override
                    public void success(Result<User> userResult) {
                        User user = userResult.data;
                        userProfile = new UserProfile();
                        userProfile.setName(user.screenName);
                        userProfile.setProfileImageUrl(user.profileImageUrlHttps);
                        profileFetched.operationFinished(userProfile);
                    }

                    @Override
                    public void failure(TwitterException e) {

                    }
                });
    }

    private void fetchFacebookUserData(final IOperationFinishWithDataCallback profileFetched) {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        userProfile = new UserProfile();
                        try {
                            StringBuilder userName = new StringBuilder();
                            if (object.has("first_name")) {
                                userName.append(object.getString("first_name"));
                            }
                            if (object.has("last_name")) {
                                userName.append(" ");
                                userName.append(object.getString("last_name"));
                            }
                            userProfile.setName(userName.toString());
                            String avatarUrl = "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large";
                            userProfile.setProfileImageUrl(avatarUrl);
                            profileFetched.operationFinished(userProfile);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,email,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
