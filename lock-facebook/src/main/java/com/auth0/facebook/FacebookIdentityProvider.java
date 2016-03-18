/*
 * FacebookIdentityProvider.java
 *
 * Copyright (c) 2014 Auth0 (http://auth0.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.auth0.facebook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.auth0.core.Application;
import com.auth0.core.Strategies;
import com.auth0.identity.IdentityProvider;
import com.auth0.identity.IdentityProviderCallback;
import com.auth0.identity.IdentityProviderRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Collection;
import java.util.Collections;

public class FacebookIdentityProvider implements IdentityProvider {
    private static final String TAG = FacebookIdentityProvider.class.getName();

    private CallbackManager callbackManager;

    private Collection<String> permissions;

    public FacebookIdentityProvider(Context context) {
        FacebookSdk.sdkInitialize(context.getApplicationContext());
        this.permissions = Collections.singletonList("public_profile");
    }

    public FacebookIdentityProvider(Context context, Collection<String> permissions) {
        this(context);
        this.permissions = permissions != null && !permissions.isEmpty() ? permissions : Collections.singletonList("public_profile");
    }

    @Override
    public void setCallback(final IdentityProviderCallback callback) {
        this.callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.v(TAG, "Logged in with permissions" + loginResult.getRecentlyGrantedPermissions());
                callback.onSuccess(Strategies.Facebook.getName(), loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                callback.onFailure(R.string.com_auth0_social_error_title, R.string.com_auth0_facebook_cancelled_error_message, null);
            }

            @Override
            public void onError(FacebookException e) {
                Log.e(FacebookIdentityProvider.class.getName(), "Failed to authenticate with FB", e);
                int messageResource = e instanceof FacebookOperationCanceledException ? R.string.com_auth0_facebook_cancelled_error_message : R.string.com_auth0_social_access_denied_message;
                callback.onFailure(R.string.com_auth0_facebook_error_title, messageResource, e);
            }
        });
    }

    @Override
    public void start(Activity activity, String serviceName) {
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
    }

    @Override
    public void start(Activity activity, IdentityProviderRequest event, Application application) {
        start(activity, Strategies.Facebook.getName());
    }

    @Override
    public void stop() {}

    @Override
    public boolean authorize(Activity activity, int requestCode, int resultCode, Intent data) {
        return IdentityProvider.WEBVIEW_AUTH_REQUEST_CODE == requestCode || callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void clearSession() {
        LoginManager.getInstance().logOut();
    }
}
