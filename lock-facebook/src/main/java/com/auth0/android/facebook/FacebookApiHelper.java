package com.auth0.android.facebook;

import android.app.Activity;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Collection;

class FacebookApiHelper {

    private CallbackManager callbackManager;

    public FacebookApiHelper(Activity activity, FacebookCallback<LoginResult> facebookCallback) {
        FacebookSdk.sdkInitialize(activity);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback);
    }

    public void login(Activity activity, Collection<String> permissions) {
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
    }

    public void logout() {
        LoginManager.getInstance().logOut();
    }

    public boolean finishLogin(int requestCode, int resultCode, Intent intent) {
        return callbackManager.onActivityResult(requestCode, resultCode, intent);
    }
}
