package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.LoginMaster;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.interfaces.ILoginManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.utils.Logger;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentProfile extends Fragment {

    public static final String TAG = "fragment_profile";
    private static final String FB_PERMISSIONS = "public_profile";
    private static final String VK_SCOPE = "email";

    private LinearLayout lnLogein;
    private LinearLayout lnLogedin;

    private LoginButton btnFacebookLogin;
    private TwitterLoginButton btnTwitter;
    private ILoginManager loginManager;
    private ProgressBar progressBar;

    private FacebookCallback facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            initUIAfterLogin();
        }

        @Override
        public void onCancel() {
            // App code
        }

        @Override
        public void onError(FacebookException exception) {
            Logger.printError("Facebook login error: ", exception.toString());
        }
    };

    private Callback<TwitterSession> tweetCallback = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            LoginMaster.getInstance().setIsLogedinWithTwitter(true);
            LoginMaster.getInstance().setTwitterToCognito(result.data);
            initUIAfterLogin();
        }

        @Override
        public void failure(TwitterException exception) {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        loginManager = (ILoginManager) getActivity();
        lnLogein = (LinearLayout) view.findViewById(R.id.lnLogin);
        lnLogedin = (LinearLayout) view.findViewById(R.id.lnLogedIn);
        progressBar = (ProgressBar) view.findViewById(R.id.pbLoading);

        progressBar.setVisibility(View.GONE);

        view.findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                initLoginUI(view);
            }
        });

        view.findViewById(R.id.btnVklogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKSdk.login(FragmentProfile.this, VK_SCOPE);
            }
        });

        if (LoginMaster.getInstance().isLogedIn()) {
            lnLogein.setVisibility(View.GONE);
            lnLogedin.setVisibility(View.VISIBLE);
        } else {
            initLoginUI(view);
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        btnTwitter.onActivityResult(requestCode, resultCode, data);
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken token) {
                lnLogein.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                LoginMaster.getInstance().setVkToCognito(token, new IOperationFinishCallback() {
                    @Override
                    public void operationFinished() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initUIAfterLogin();
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(VKError error) {
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Should be called after login, will sync all changes with cloud and update UI after it.
     */
    private void initUIAfterLogin() {
        lnLogein.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        ProfileManager.getInstance().syncAllChanges(new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        lnLogedin.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    public void initLoginUI(View rootView) {
        lnLogein.setVisibility(View.VISIBLE);
        lnLogedin.setVisibility(View.GONE);
        btnFacebookLogin = (LoginButton) rootView.findViewById(R.id.btnFacebookLogin);
        btnTwitter = (TwitterLoginButton) rootView.findViewById(R.id.btnTwitterLogin);
        btnFacebookLogin.setReadPermissions(FB_PERMISSIONS);
        btnFacebookLogin.registerCallback(loginManager.getFacebookCallbackManager(), facebookCallback);
        btnTwitter.setCallback(tweetCallback);
    }
}
