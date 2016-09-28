package com.auth0.android.facebook;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.provider.AuthHandler;
import com.auth0.android.provider.AuthProvider;

public class FacebookAuthHandler implements AuthHandler {

    private final FacebookAuthProvider provider;

    public FacebookAuthHandler(@NonNull AuthenticationAPIClient apiClient) {
        this(new FacebookAuthProvider(apiClient));
    }

    public FacebookAuthHandler(@NonNull FacebookAuthProvider provider) {
        this.provider = provider;
    }

    @Nullable
    @Override
    public AuthProvider providerFor(@NonNull String strategy, @NonNull String connection) {
        if ("facebook".equals(strategy)) {
            return provider;
        }
        return null;
    }
}