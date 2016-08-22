package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.adaperts.ProfileItemsAdapter;
import com.chickenkiller.upods2.controllers.app.LoginMaster;
import com.chickenkiller.upods2.controllers.internet.SyncMaster;
import com.chickenkiller.upods2.interfaces.IContextMenuManager;
import com.chickenkiller.upods2.interfaces.IControlStackHistory;
import com.chickenkiller.upods2.interfaces.IFragmentsManager;
import com.chickenkiller.upods2.interfaces.ILoginManager;
import com.chickenkiller.upods2.interfaces.IOperationFinishCallback;
import com.chickenkiller.upods2.interfaces.IOperationFinishWithDataCallback;
import com.chickenkiller.upods2.interfaces.ISlidingMenuHolder;
import com.chickenkiller.upods2.interfaces.IToolbarHolder;
import com.chickenkiller.upods2.models.ProfileItem;
import com.chickenkiller.upods2.models.UserProfile;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.decorators.DelayedOnClickListener;
import com.chickenkiller.upods2.utils.enums.ContextMenuType;
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
 * Created by Alon Zilberman on 8/8/15.
 */
public class FragmentProfile extends Fragment implements IControlStackHistory {

    public static final String TAG = "fragment_profile";
    private static final String FB_PERMISSIONS = "public_profile";
    private static final String VK_SCOPE = "email";

    private LinearLayout lnLoggedInState;
    private LinearLayout lnLoggedOutState;

    private Button btnFacebookLogin;
    private Button btnTwitter;
    private ILoginManager loginManager;
    private ProgressBar progressBar;
    private TwitterAuthClient mTwitterAuthClient;
    private boolean skipAddingToHistory;

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
        ((IToolbarHolder) getActivity()).getToolbar().setVisibility(View.GONE);

        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        loginManager = (ILoginManager) getActivity();
        lnLoggedInState = (LinearLayout) rootView.findViewById(R.id.lnLoggedInState);
        lnLoggedOutState = (LinearLayout) rootView.findViewById(R.id.lnLoggedOutState);
        progressBar = (ProgressBar) rootView.findViewById(R.id.pbLoading);

        progressBar.setVisibility(View.GONE);

        rootView.findViewById(R.id.btnVklogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKSdk.login(FragmentProfile.this, VK_SCOPE);
            }
        });

        if (LoginMaster.getInstance().isLogedIn()) {
            lnLoggedOutState.setVisibility(View.GONE);
            lnLoggedInState.setVisibility(View.VISIBLE);
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
        lnLoggedOutState.setVisibility(View.GONE);
        lnLoggedInState.setVisibility(View.VISIBLE);
        initUserProfileUI();
    }

    private void initUserProfileUI() {
        final ImageView imgAvatar = (ImageView) rootView.findViewById(R.id.imgAvatar);
        LoginMaster.getInstance().initUserProfile(new IOperationFinishWithDataCallback() {
            @Override
            public void operationFinished(Object data) {
                UserProfile userProfile = (UserProfile) data;
                ((TextView) rootView.findViewById(R.id.tvUserName)).setText(((UserProfile) data).getName());
                Glide.with(getActivity()).load(userProfile.getProfileImageUrl()).asBitmap().centerCrop().into(new BitmapImageViewTarget(imgAvatar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imgAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                });
                ((ISlidingMenuHolder) getActivity()).getSlidingMenu().updateHeader(userProfile);
            }
        }, true);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View lvHeaderView = inflater.inflate(R.layout.profile_item_header, null, false);

        //Foooter
        View lvFooterView = inflater.inflate(R.layout.profile_footer, null, false);
        Button btnSave = (Button) lvFooterView.findViewById(R.id.btnSaveToCloud);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync(SyncMaster.Task.PUSH);
            }
        });
        Button btnRestore = (Button) lvFooterView.findViewById(R.id.btnRestoreFromCloud);
        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync(SyncMaster.Task.PULL);
            }
        });
        //End footer

        ListView lvProfile = (ListView) rootView.findViewById(R.id.lvProfile);
        if (lvProfile.getHeaderViewsCount() == 0) {
            lvProfile.addHeaderView(lvHeaderView);
        }
        if (lvProfile.getFooterViewsCount() == 0) {
            lvProfile.addFooterView(lvFooterView);
        }

        lvProfile.setHeaderDividersEnabled(false);
        lvProfile.setFooterDividersEnabled(false);
        ProfileItemsAdapter profileItemsAdapter = new ProfileItemsAdapter(getActivity(), R.layout.profile_item);
        profileItemsAdapter.addAll(ProfileItem.fromLoggedinUser(getActivity()));
        lvProfile.setAdapter(profileItemsAdapter);

        rootView.findViewById(R.id.imgProfileArrowLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        rootView.findViewById(R.id.imgProfileDots).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((IContextMenuManager) getActivity()).openContextMenu(v, ContextMenuType.PROFILE,
                        null, new IOperationFinishCallback() {
                            @Override
                            public void operationFinished() {
                                FragmentProfile fragmentProfile = new FragmentProfile();
                                fragmentProfile.skipAddingToHistory = true;
                                ((IFragmentsManager) getActivity()).showFragment(R.id.fl_content, fragmentProfile, TAG);
                            }
                        });
            }
        });
    }

    public void initLoginUI(View rootView) {
        lnLoggedOutState.setVisibility(View.VISIBLE);
        lnLoggedInState.setVisibility(View.GONE);
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

    private void sync(SyncMaster.Task task) {
        final MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.syncing)
                .content(R.string.please_wait)
                .progress(true, 0)
                .show();
        LoginMaster.getInstance().syncWithCloud(new IOperationFinishCallback() {
            @Override
            public void operationFinished() {
                initUIAfterLogin();
                progressDialog.dismiss();
            }
        }, task);
    }

    @Override
    public boolean shouldBeAddedToStack() {
        return !skipAddingToHistory;
    }

}
