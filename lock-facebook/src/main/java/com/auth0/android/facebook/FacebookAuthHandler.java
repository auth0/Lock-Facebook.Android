package com.auth0.android.facebook;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.provider.AuthHandler;
import com.auth0.android.provider.AuthProvider;

/**
 * Default Auth handler for Lock that will make all facebook auth request authenticate using the native FB SDK
 */
public class FacebookAuthHandler implements AuthHandler {

    private final FacebookAuthProvider provider;

    /**
     * Creates a Facebook Auth handler that will make all auth request for the strategy facebook
     * @param apiClient Auth0 API client
     */
    public FacebookAuthHandler(@NonNull AuthenticationAPIClient apiClient) {
        this(new FacebookAuthProvider(apiClient));
    }

    /**
     * Creates a Facebook Auth handler that will make all auth request for the strategy facebook and return the given provider
     * @param provider that will handle FB authentication
     */
    public FacebookAuthHandler(@NonNull FacebookAuthProvider provider) {
        this.provider = provider;
    }

    @Nullable
    @Override
    public AuthProvider providerFor(@Nullable String strategy, @NonNull String connection) {
        if ("facebook".equals(strategy)) {
            return provider;
        }
        return null;
    }
}