package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.LoginMaster;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.interfaces.ILoginManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.UserProfile;
import com.chickenkiller.upods2.utils.Logger;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.util.Arrays;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentProfile extends Fragment {

    public static final String TAG = "fragment_profile";
    private static final String FB_PERMISSIONS = "public_profile";
    private static final String VK_SCOPE = "email";

    private LinearLayout lnLogein;
    private LinearLayout lnUsetLogedIn;

    private Button btnFacebookLogin;
    private Button btnTwitter;
    private ILoginManager loginManager;
    private ProgressBar progressBar;
    private TwitterAuthClient mTwitterAuthClient;

    private View rootView;

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
            initUIAfterLogin();
        }

        @Override
        public void failure(TwitterException exception) {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTwitterAuthClient = new TwitterAuthClient();

        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        loginManager = (ILoginManager) getActivity();
        lnLogein = (LinearLayout) rootView.findViewById(R.id.lnLogin);
        lnUsetLogedIn = (LinearLayout) rootView.findViewById(R.id.lnLogedIn);
        progressBar = (ProgressBar) rootView.findViewById(R.id.pbLoading);

        progressBar.setVisibility(View.GONE);

        rootView.findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginMaster.getInstance().logout();
                ((ISlidingMenuHolder) getActivity()).getSlidingMenu().updateHeader(true);
                initLoginUI(rootView);
            }
        });

        rootView.findViewById(R.id.btnVklogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKSdk.login(FragmentProfile.this, VK_SCOPE);
            }
        });

        if (LoginMaster.getInstance().isLogedIn()) {
            lnLogein.setVisibility(View.GONE);
            lnUsetLogedIn.setVisibility(View.VISIBLE);
            initUserProfileUI();
        } else {
            initLoginUI(rootView);
        }
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken token) {
                lnLogein.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                initUIAfterLogin();
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
        LoginMaster.getInstance().syncWithCloud(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        lnUsetLogedIn.setVisibility(View.VISIBLE);
                        initUserProfileUI();
                    }
                });
            }
        });
        ((ISlidingMenuHolder) getActivity()).getSlidingMenu().updateHeader(true);
    }

    private void initUserProfileUI() {
        Toolbar toolbar = ((IToolbarHolder) getActivity()).getToolbar();
        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setTitle(R.string.profile_my_profile);
            toolbar.findViewById(R.id.action_search).setVisibility(View.GONE);
        }
        final ImageView imgAvatar = (ImageView) rootView.findViewById(R.id.imgAvatar);
        LoginMaster.getInstance().initUserProfile(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                UserProfile userProfile = (UserProfile) data;
                Glide.with(getActivity()).load(userProfile.getProfileImageUrl()).asBitmap().centerCrop().into(new BitmapImageViewTarget(imgAvatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imgAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
        }, false);

        int radioCount = ProfileManager.getInstance().getSubscribedRadioItems().size();
        int podcastsCount = ProfileManager.getInstance().getSubscribedPodcasts().size();

        TextView tvRadioCount = (TextView) rootView.findViewById(R.id.tvRadioCount);
        tvRadioCount.setText(Html.fromHtml(buildCountString(tvRadioCount.getText().toString(), radioCount)));

        TextView tvPodcastsCount = (TextView) rootView.findViewById(R.id.tvPodcastsCount);
        tvPodcastsCount.setText(Html.fromHtml(buildCountString(tvPodcastsCount.getText().toString(), podcastsCount)));
    }

    private String buildCountString(String text, int count) {
        StringBuilder countsBuilder = new StringBuilder();
        countsBuilder.append("<b>");
        countsBuilder.append(text);
        countsBuilder.append("</b>");
        countsBuilder.append(count);
        return countsBuilder.toString();
    }

    public void initLoginUI(View rootView) {
        ((IToolbarHolder) getActivity()).getToolbar().setVisibility(View.GONE);
        lnLogein.setVisibility(View.VISIBLE);
        lnUsetLogedIn.setVisibility(View.GONE);
        btnFacebookLogin = (Button) rootView.findViewById(R.id.btnFacebookLogin);
        btnTwitter = (Button) rootView.findViewById(R.id.btnTwitterLogin);
        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().registerCallback(loginManager.getFacebookCallbackManager(), facebookCallback);
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile"));
            }
        });
        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTwitterAuthClient.authorize(getActivity(), tweetCallback);
            }
        });
    }

}
