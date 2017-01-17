package com.auth0.android.facebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.AuthenticationCallback;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.AuthProvider;
import com.auth0.android.result.Credentials;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;

import java.util.Collection;
import java.util.Collections;

/**
 * Native Facebook Sign In implementation of the Auth0 AuthProvider.
 */
public class FacebookAuthProvider extends AuthProvider {

    private static final String TAG = FacebookAuthProvider.class.getSimpleName();
    private final AuthenticationAPIClient auth0;
    private final String connectionName;
    private final FacebookApi facebook;

    private Collection<String> permissions;
    private boolean rememberLastLogin;

    /**
     * Creates a new Facebook Auth provider for the default Facebook connection
     * @param connectionName name of the connection used to authenticate with Auth0
     * @param auth0 an Auth0 AuthenticationAPIClient instance
     */
    public FacebookAuthProvider(String connectionName, AuthenticationAPIClient auth0) {
        this(connectionName, auth0, new FacebookApi());
    }

    FacebookAuthProvider(String connectionName, AuthenticationAPIClient auth0, FacebookApi facebook) {
        this.auth0 = auth0;
        this.connectionName = connectionName;
        this.facebook = facebook;
        this.permissions = Collections.singleton("public_profile");
        this.rememberLastLogin = true;
    }

    /**
     * Creates a new Facebook Auth provider for the default Facebook connection
     * @param auth0 an Auth0 AuthenticationAPIClient instance
     */
    public FacebookAuthProvider(@NonNull AuthenticationAPIClient auth0) {
        this("facebook", auth0);
    }

    /**
     * Change the scope to request on the user login. Use any of the permissions defined in https://developers.facebook.com/docs/facebook-login/android/permissions. Must be called before start().
     * The permission "public_profile" is requested by default.
     *
     * @param permissions the permissions to add to the request
     */
    public void setPermissions(@NonNull Collection<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * Whether it should remember the last account used to log in or it should ask the user to input his credentials.
     * By default it's true, meaning it will not ask for the user account if he's already logged in.
     *
     * @param rememberLastLogin flag to remember last Facebook login
     */
    public void rememberLastLogin(boolean rememberLastLogin) {
        this.rememberLastLogin = rememberLastLogin;
    }

    @Override
    protected void requestAuth(Activity activity, int requestCode) {
        if (!this.rememberLastLogin) {
            facebook.logout();
        }
        facebook.login(activity, requestCode, permissions, createFacebookCallback());
    }

    @Override
    public boolean authorize(int requestCode, int resultCode, @Nullable Intent intent) {
        return facebook.finishLogin(requestCode, resultCode, intent);
    }

    @Override
    public boolean authorize(@Nullable Intent intent) {
        //Unused
        return false;
    }

    @Override
    public String[] getRequiredAndroidPermissions() {
        return new String[0];
    }

    @Override
    public void stop() {
        super.stop();
        clearSession();
    }

    @Override
    public void clearSession() {
        super.clearSession();
        facebook.logout();
    }

    Collection<String> getPermissions() {
        return permissions;
    }

    String getConnection() {
        return connectionName;
    }

    private FacebookApi.Callback createFacebookCallback() {
        final AuthCallback callback = getSafeCallback();
        return new FacebookApi.Callback() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (loginResult.getRecentlyDeniedPermissions().isEmpty()) {
                    requestAuth0Token(loginResult.getAccessToken().getToken());
                } else {
                    Log.w(TAG, "Some permissions were not granted: " + loginResult.getRecentlyDeniedPermissions().toString());
                    callback.onFailure(new AuthenticationException("Some of the requested permissions were not granted by the user."));
                }
            }

            @Override
            public void onCancel() {
                Log.w(TAG, "User cancelled the log in dialog");
                callback.onFailure(new AuthenticationException("User cancelled the authentication consent dialog."));
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "Error on log in: " + error.getMessage());
                callback.onFailure(new AuthenticationException(error.getMessage()));
            }
        };
    }

    private void requestAuth0Token(String token) {
        final AuthCallback callback = getSafeCallback();
        auth0.loginWithOAuthAccessToken(token, connectionName)
                .addAuthenticationParameters(getParameters())
                .start(new AuthenticationCallback<Credentials>() {
                    @Override
                    public void onSuccess(Credentials credentials) {
                        callback.onSuccess(credentials);
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        callback.onFailure(error);
                    }
                });
    }


    private AuthCallback getSafeCallback() {
        final AuthCallback callback = getCallback();
        return callback != null ? callback : new AuthCallback() {
            @Override
            public void onFailure(@NonNull Dialog dialog) {
                Log.w(TAG, "Called authorize with no callback defined");
            }

            @Override
            public void onFailure(AuthenticationException exception) {
                Log.w(TAG, "Called authorize with no callback defined");
            }

            @Override
            public void onSuccess(@NonNull Credentials credentials) {
                Log.w(TAG, "Called authorize with no callback defined");
            }
        };
    }
}
