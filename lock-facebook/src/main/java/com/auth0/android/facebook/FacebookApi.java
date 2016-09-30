package com.auth0.android.facebook;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Collection;

class FacebookApi {

    private final CallbackManager callbackManager;

    public FacebookApi() {
        callbackManager = CallbackManager.Factory.create();
    }

    public void login(Activity activity, int requestCode, Collection<String> permissions, final Callback callback) {
        if (FacebookSdk.isInitialized()) {
            Log.w("FacebookAuthProvider", "FacebookSDK was already initialized and we couldn't set the custom Request Code for the Login result. This may affect Android.Lock Library inner workings.");
            Log.w("FacebookAuthProvider", "Either initialize the SDK with the Request Code as the callbackRequestCodeOffset, or let this provider initialize the SDK.");
        } else {
            FacebookSdk.sdkInitialize(activity, requestCode);
        }
        //Use callbackRequestCodeOffset as the requestCode because Login RC is internally defined as "offset + 0".
        //see https://github.com/facebook/facebook-android-sdk/blob/master/facebook/src/main/java/com/facebook/internal/CallbackManagerImpl.java#L92
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
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
    }

    public void logout() {
        if (FacebookSdk.isInitialized()){
            LoginManager.getInstance().logOut();
        } else {
            Log.w("FacebookAuthProvider", "Couldn't log out as the SDK wasn't initialized yet.");
        }
    }

    public boolean finishLogin(int requestCode, int resultCode, Intent intent) {
        return callbackManager.onActivityResult(requestCode, resultCode, intent);
    }

    interface Callback {
        void onSuccess(LoginResult result);
        void onCancel();
        void onError(FacebookException exception);
    }
}
