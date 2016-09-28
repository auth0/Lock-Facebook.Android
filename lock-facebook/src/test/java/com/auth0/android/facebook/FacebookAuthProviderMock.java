package com.auth0.android.facebook;

import android.support.annotation.NonNull;

import com.auth0.android.authentication.AuthenticationAPIClient;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginResult;

public class FacebookAuthProviderMock extends FacebookAuthProvider {

    private final FacebookApiHelper apiHelper;
    FacebookCallback<LoginResult> facebookCallback;
    private boolean logoutBeforeLogin;

    /**
     * @param client an Auth0 AuthenticationAPIClient instance
     */
    public FacebookAuthProviderMock(@NonNull AuthenticationAPIClient client, @NonNull FacebookApiHelper apiHelper) {
        super(client);
        this.apiHelper = apiHelper;
    }

    @Override
    FacebookApiHelper createApiHelper(boolean logoutBeforeLogin) {
        createFacebookCallback();
        this.logoutBeforeLogin = logoutBeforeLogin;
        return apiHelper;
    }

    @Override
    FacebookCallback<LoginResult> createFacebookCallback() {
        facebookCallback = super.createFacebookCallback();
        return facebookCallback;
    }

    boolean willLogoutBeforeLogin() {
        return logoutBeforeLogin;
    }
}
