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

import io.fabric.sdk.android.Fabric;

/**
 * Created by alonzilberman on 11/29/15.
 */
public class LoginMaster {

    private static final String LOG_TAG = "LoginMaster";
    private static final String TWITTER_CONSUMER_KEY = "wr8t6lPMxtC09uMpIEayM5FBC";
    private static final String TWITTER_CONSUMER_SECRET = "dtnTy4RQfnowu60XGHToj830j4AYsKxDA82PWZBijgSdk0gnlk";

    private static LoginMaster loginMaster;


    private UserProfile userProfile;

    private LoginMaster() {
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

        initFacebook();
        initTwitter();
        initVkontakte();
    }

    private void initFacebook() {
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                Logger.printInfo(LOG_TAG, "Is loged in with facebook");
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
                        if (((JSONObject) data).getJSONObject("result").has("profile")) {
                            JSONObject profile = new JSONObject(((JSONObject) data).getJSONObject("result").getString("profile"));
                            ProfileManager.getInstance().readFromJson(profile);
                        }
                        if (((JSONObject) data).getJSONObject("result").has("settings")) {
                            JSONObject settings = new JSONObject(((JSONObject) data).getJSONObject("result").getString("settings"));
                            SettingsManager.getInstace().readSettings(settings);
                        }
                        SyncMaster profileSyncMaster = new SyncMaster(getLoginType(), getToken(), SyncMaster.TASK_SYNC);
                        profileSyncMaster.setProfileSyncedCallback(iOperationFinishCallback);
                        profileSyncMaster.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            SyncMaster profileSyncMaster = new SyncMaster(getLoginType(), getToken());
            profileSyncMaster.setProfileSyncedCallback(syncFinishedCallback);
            profileSyncMaster.execute();
        }
    }

    public String getLoginType() {
        if (AccessToken.getCurrentAccessToken() != null)
            return SyncMaster.TYPE_FB;
        else if (Twitter.getSessionManager().getActiveSession() != null) {
            return SyncMaster.TYPE_TWITTER;
        } else
            return SyncMaster.TYPE_VK;
    }

    public String getToken() {
        if (AccessToken.getCurrentAccessToken() != null)
            return AccessToken.getCurrentAccessToken().getToken();
        else if (Twitter.getSessionManager().getActiveSession() != null) {
            TwitterSession session = Twitter.getSessionManager().getActiveSession();
            TwitterAuthToken authToken = session.getAuthToken();
            return authToken.token;
        } else
            return VKAccessToken.currentToken().accessToken;
    }


    public boolean isLogedIn() {
        return AccessToken.getCurrentAccessToken() != null || Twitter.getSessionManager().getActiveSession() != null || VKSdk.isLoggedIn();
    }

    public void logout() {
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
            Logger.printInfo(LOG_TAG, "Loged out from facebook");
        } else if (Twitter.getSessionManager().getActiveSession() != null) {
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
            Logger.printInfo(LOG_TAG, "Loged out from twitter");
        } else if (VKSdk.isLoggedIn()) {
            VKSdk.logout();
            Logger.printInfo(LOG_TAG, "Loged out from vk");
        }
        userProfile = new UserProfile();
    }

    public void initUserProfile(final IOperationFinishWithDataCallback profileFetched, boolean isForceUpdate) {
        if (userProfile != null && !isForceUpdate) {
            profileFetched.operationFinished(userProfile);
        } else if (!isLogedIn()) {
            userProfile = new UserProfile();
            profileFetched.operationFinished(userProfile);
        } else {
            if (AccessToken.getCurrentAccessToken() != null) {
                fetchFacebookUserData(profileFetched);
            } else if (Twitter.getSessionManager().getActiveSession() != null) {
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
