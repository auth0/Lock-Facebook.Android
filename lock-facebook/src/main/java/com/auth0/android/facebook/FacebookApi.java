package com.auth0.android.facebook;

import android.app.Activity;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Collection;

class FacebookApi {

    public static final String TAG = "FacebookAuthProvider";
    private final CallbackManager callbackManager;

    public FacebookApi() {
        callbackManager = CallbackManager.Factory.create();
    }

    public void login(Activity activity, int requestCode, Collection<String> permissions, final Callback callback) {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                callback.onSuccess(result);
            }

            @Override
            public void onCancel() {
                callback.onCancel();
            }

            @Override
            public void onError(FacebookException error) {
                callback.onError(error);
            }
        });

        FacebookSignInDelegateActivity.signIn(activity, requestCode, permissions);
    }

    public void logout() {
        LoginManager.getInstance().logOut();
    }

    public boolean finishLogin(int requestCode, int resultCode, Intent intent) {
        return FacebookSignInDelegateActivity.finishSignIn(callbackManager, requestCode, resultCode, intent);
    }

    interface Callback {
        void onSuccess(LoginResult result);

        void onCancel();

        void onError(FacebookException exception);
    }
}
