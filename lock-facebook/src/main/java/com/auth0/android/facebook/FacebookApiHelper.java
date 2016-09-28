package com.auth0.android.facebook;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Collection;

class FacebookApiHelper {

    private final FacebookCallback<LoginResult> facebookCallback;
    private CallbackManager callbackManager;
    private boolean forceRequestAccount;

    public FacebookApiHelper(FacebookCallback<LoginResult> facebookCallback) {
        this.facebookCallback = facebookCallback;
    }

    public void login(Activity activity, int requestCode, Collection<String> permissions) {
        if (FacebookSdk.isInitialized()) {
            Log.w("FacebookAuthProvider", "FacebookSDK was already initialized and we couldn't set the custom Request Code for the Login result. This may affect Android.Lock Library inner workings.");
            Log.w("FacebookAuthProvider", "Either initialize the SDK with the Request Code as the callbackRequestCodeOffset, or let this provider initialize the SDK.");
        } else {
            FacebookSdk.sdkInitialize(activity, requestCode);
        }
        if (forceRequestAccount){
            logout();
        }
        //Use callbackRequestCodeOffset as the requestCode because Login RC is internally defined as "offset + 0".
        //see https://github.com/facebook/facebook-android-sdk/blob/master/facebook/src/main/java/com/facebook/internal/CallbackManagerImpl.java#L92
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback);
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
    }

    /**
     * Whether it should clear the session and logout any existing user before trying to authenticate or not.
     *
     * @param forceRequestAccount the new force flag value.
     */
    public void forceRequestAccount(boolean forceRequestAccount) {
        this.forceRequestAccount = forceRequestAccount;
    }

    public void logout() {
        LoginManager.getInstance().logOut();
    }

    public boolean finishLogin(int requestCode, int resultCode, Intent intent) {
        return callbackManager.onActivityResult(requestCode, resultCode, intent);
    }
}
