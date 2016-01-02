package com.chickenkiller.upods2.controllers.app;

import android.os.Bundle;

import com.chickenkiller.upods2.controllers.internet.SyncMaster;
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

import io.fabric.sdk.android.Fabric;

/**
 * Created by alonzilberman on 11/29/15.
 */
public class LoginMaster {

    public static final String TWITTER_TOKEN = "twitter_token";

    private static final String LOG_TAG = "LoginMaster";
    private static final String TWITTER_CONSUMER_KEY = "wr8t6lPMxtC09uMpIEayM5FBC";
    private static final String TWITTER_CONSUMER_SECRET = "dtnTy4RQfnowu60XGHToj830j4AYsKxDA82PWZBijgSdk0gnlk";

    private static LoginMaster loginMaster;
    
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
            loginMaster.syncCounter = 0;
        }
        return loginMaster;
    }

    public void init() {
        //First init VKSdk because it is used by vkAuthenticationProvider
        VKSdk.initialize(UpodsApplication.getContext());

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
                }
            }
        };
        vkAccessTokenTracker.startTracking();
    }


    public void syncWithCloud(final IOperationFinishWithDataCallback iOperationFinishCallback) {
        if (isLogedIn()) {
            IOperationFinishWithDataCallback syncFinishedCallback = new IOperationFinishWithDataCallback() {
                @Override
                public void operationFinished(Object data) {
                    try {
                        ProfileManager.getInstance().readFromJson(((JSONObject) data).getJSONObject("profile"));
                        ProfileManager.getInstance().readFromJson(((JSONObject) data).getJSONObject("settings"));
                        SyncMaster profileSyncMaster = new SyncMaster(getLoginType(), getToken(), SyncMaster.TASK_SYNC);
                        profileSyncMaster.setProfileSyncedCallback(iOperationFinishCallback);
                        profileSyncMaster.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            if (isLogedinWithFacebook) {
                SyncMaster profileSyncMaster = new SyncMaster(getLoginType(), getToken());
                profileSyncMaster.setProfileSyncedCallback(syncFinishedCallback);
                profileSyncMaster.execute();
            }
        }
    }

    public String getLoginType() {
        if (isLogedinWithFacebook)
            return SyncMaster.TYPE_FB;
        else if (isLogedinWithTwitter) {
            return SyncMaster.TYPE_TWITTER;
        } else
            return SyncMaster.TYPE_VK;
    }

    public String getToken() {
        if (isLogedinWithFacebook)
            return AccessToken.getCurrentAccessToken().getToken();
        else if (isLogedinWithTwitter)
            return Prefs.getString(TWITTER_TOKEN, "");
        else
            return VKAccessToken.currentToken().accessToken;
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
