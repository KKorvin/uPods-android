package com.chickenkiller.upods2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chickenkiller.upods2.R;
import com.chickenkiller.upods2.controllers.app.LoginMaster;
import com.chickenkiller.upods2.controllers.app.ProfileManager;
import com.chickenkiller.upods2.interfaces.ILoginManager;
import com.chickenkiller.upods2.utils.Logger;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Created by alonzilberman on 8/8/15.
 */
public class FragmentProfile extends Fragment {

    public static final String TAG = "fragment_profile";
    private static final String FB_PERMISSIONS = "public_profile";

    private LinearLayout lnLogein;
    private LinearLayout lnLogedin;
    private LoginButton btnFacebookLogin;
    private ILoginManager loginManager;

    private FacebookCallback facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            lnLogein.setVisibility(View.GONE);
            lnLogedin.setVisibility(View.VISIBLE);
            ProfileManager.getInstance().syncAllChanges();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        loginManager = (ILoginManager) getActivity();
        lnLogein = (LinearLayout) view.findViewById(R.id.lnLogin);
        lnLogedin = (LinearLayout) view.findViewById(R.id.lnLogedIn);

        view.findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                initLoginUI(view);
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

    public void initLoginUI(View rootView) {
        lnLogein.setVisibility(View.VISIBLE);
        lnLogedin.setVisibility(View.GONE);
        btnFacebookLogin = (LoginButton) rootView.findViewById(R.id.btnFacebookLogin);
        btnFacebookLogin.setReadPermissions(FB_PERMISSIONS);
        btnFacebookLogin.registerCallback(loginManager.getFacebookCallbackManager(), facebookCallback);
    }
}
