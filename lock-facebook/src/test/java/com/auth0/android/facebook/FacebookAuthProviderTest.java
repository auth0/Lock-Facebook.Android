package com.auth0.android.facebook;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.request.AuthenticationRequest;
import com.auth0.android.result.Credentials;
import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;

import org.hamcrest.collection.IsArrayWithSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.noMoreInteractions;

public class FacebookAuthProviderTest {

    private static final int AUTH_REQ_CODE = 123;
    private static final int PERMISSION_REQ_CODE = 122;
    private static final String CONNECTION_NAME = "facebook";
    private static final String TOKEN = "a.raNDom.TokeN";

    @Mock
    private AuthenticationAPIClient client;
    @Mock
    private AuthCallback callback;
    @Mock
    private Activity activity;
    @Mock
    private FacebookApi apiHelper;
    @Mock
    private AuthenticationRequest request;
    @Mock
    private Credentials credentials;
    @Mock
    private Intent intent;

    private FacebookAuthProvider provider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        provider = new FacebookAuthProvider("facebook", client, apiHelper);
    }

    @Test
    public void shouldSetPermissions() throws Exception {
        provider.setPermissions(Arrays.asList("profile", "email", "account"));

        assertThat(provider.getPermissions(), is(hasSize(3)));
        assertThat(provider.getPermissions(), hasItem("account"));
        assertThat(provider.getPermissions(), hasItem("email"));
        assertThat(provider.getPermissions(), hasItem("profile"));
    }

    @Test
    public void shouldHaveNonNullPermissions() throws Exception {
        assertThat(provider.getPermissions(), is(notNullValue()));
    }

    @Test
    public void shouldHaveDefaultPermissions() throws Exception {
        assertThat(provider.getPermissions(), is(hasSize(1)));
        assertThat(provider.getPermissions(), hasItem("public_profile"));
    }

    @Test
    public void shouldSetConnectionName() throws Exception {
        provider = new FacebookAuthProvider("my-custom-connection", client, apiHelper);

        assertThat(provider.getConnection(), is("my-custom-connection"));
    }

    @Test
    public void shouldHaveNonNullConnectionName() throws Exception {
        assertThat(provider.getConnection(), is(notNullValue()));
    }

    @Test
    public void shouldHaveDefaultConnectionName() throws Exception {
        assertThat(provider.getConnection(), is(CONNECTION_NAME));
    }

    @Test
    public void shouldRequestLoginWhenStarted() throws Exception {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        verify(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), eq(provider.getPermissions()), any(FacebookApi.Callback.class));
    }

    @Test
    public void shouldParseAuthorization() throws Exception {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.authorize(AUTH_REQ_CODE, Activity.RESULT_OK, intent);
        provider.authorize(AUTH_REQ_CODE, Activity.RESULT_CANCELED, intent);

        verify(apiHelper).finishLogin(AUTH_REQ_CODE, Activity.RESULT_OK, intent);
        verify(apiHelper).finishLogin(AUTH_REQ_CODE, Activity.RESULT_CANCELED, intent);
    }

    @Test
    public void shouldReturnDelegatedParseResult() throws Exception {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        when(apiHelper.finishLogin(AUTH_REQ_CODE, Activity.RESULT_OK, intent)).thenReturn(true);
        when(apiHelper.finishLogin(999, Activity.RESULT_OK, intent)).thenReturn(false);

        assertThat(provider.authorize(AUTH_REQ_CODE, Activity.RESULT_OK, intent), is(true));
        assertThat(provider.authorize(999, Activity.RESULT_OK, intent), is(false));
    }

    @Test
    public void shouldDoNothingWhenCalledFromNewIntent() throws Exception {
        assertThat(provider.authorize(null), is(false));
    }

    @Test
    public void shouldLogoutBeforeLoginByDefault() throws Exception {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        verify(apiHelper).logout();
    }

    @Test
    public void shouldLogoutBeforeLoginIfRequested() throws Exception {
        provider.rememberLastLogin(false);
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        verify(apiHelper).logout();
    }

    @Test
    public void shouldNotRequireAndroidPermissions() throws Exception {
        assertThat(provider.getRequiredAndroidPermissions(), is(notNullValue()));
        assertThat(provider.getRequiredAndroidPermissions(), is(IsArrayWithSize.<String>emptyArray()));
    }

    @Test
    public void shouldNotCallAuth0OAuthEndpointWhenSomePermissionsWereRejected() throws Exception {
        when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME)).thenReturn(request);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final FacebookApi.Callback callback = (FacebookApi.Callback) invocation.getArguments()[3];
                callback.onSuccess(createLoginResultFromToken(TOKEN, false));
                return null;
            }
        }).when(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), anyCollectionOf(String.class), any(FacebookApi.Callback.class));
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        verify(client, noMoreInteractions()).loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME);
    }

    @Test
    public void shouldFailWithTextWhenSomePermissionsWereRejected() throws Exception {
        when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME)).thenReturn(request);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final FacebookApi.Callback callback = (FacebookApi.Callback) invocation.getArguments()[3];
                callback.onSuccess(createLoginResultFromToken(TOKEN, false));
                return null;
            }
        }).when(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), anyCollectionOf(String.class), any(FacebookApi.Callback.class));
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        ArgumentCaptor<AuthenticationException> throwableCaptor = ArgumentCaptor.forClass(AuthenticationException.class);
        verify(callback).onFailure(throwableCaptor.capture());
        final AuthenticationException exception = throwableCaptor.getValue();
        assertThat(exception, is(notNullValue()));
        assertThat(exception.getMessage(), is("Some of the requested permissions were not granted by the user."));
    }

    @Test
    public void shouldCallAuth0OAuthEndpointWhenFacebookTokenIsReceived() throws Exception {
        when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME)).thenReturn(request);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final FacebookApi.Callback callback = (FacebookApi.Callback) invocation.getArguments()[3];
                callback.onSuccess(createLoginResultFromToken(TOKEN, true));
                return null;
            }
        }).when(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), anyCollectionOf(String.class), any(FacebookApi.Callback.class));
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        verify(client).loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME);
    }

    @Test
    public void shouldCallAuth0OAuthEndpointWithCustomConnectionNameWhenGoogleTokenIsReceived() throws Exception {
        provider = new FacebookAuthProvider("my-custom-connection", client, apiHelper);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final FacebookApi.Callback callback = (FacebookApi.Callback) invocation.getArguments()[3];
                callback.onSuccess(createLoginResultFromToken(TOKEN, true));
                return null;
            }
        }).when(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), anyCollectionOf(String.class), any(FacebookApi.Callback.class));
        when(client.loginWithOAuthAccessToken(TOKEN, "my-custom-connection")).thenReturn(request);
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        verify(client).loginWithOAuthAccessToken(TOKEN, "my-custom-connection");
    }

    @Test
    public void shouldFailWithTextWhenFacebookRequestFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final FacebookApi.Callback callback = (FacebookApi.Callback) invocation.getArguments()[3];
                callback.onError(new FacebookException("facebook error"));
                return null;
            }
        }).when(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), anyCollectionOf(String.class), any(FacebookApi.Callback.class));
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        ArgumentCaptor<AuthenticationException> throwableCaptor = ArgumentCaptor.forClass(AuthenticationException.class);
        verify(callback).onFailure(throwableCaptor.capture());
        final AuthenticationException exception = throwableCaptor.getValue();
        assertThat(exception, is(notNullValue()));
        assertThat(exception.getMessage(), is("facebook error"));
    }

    @Test
    public void shouldFailWithTextWhenFacebookRequestIsCancelled() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final FacebookApi.Callback callback = (FacebookApi.Callback) invocation.getArguments()[3];
                callback.onCancel();
                return null;
            }
        }).when(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), anyCollectionOf(String.class), any(FacebookApi.Callback.class));
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        ArgumentCaptor<AuthenticationException> throwableCaptor = ArgumentCaptor.forClass(AuthenticationException.class);
        verify(callback).onFailure(throwableCaptor.capture());
        final AuthenticationException exception = throwableCaptor.getValue();
        assertThat(exception, is(notNullValue()));
        assertThat(exception.getMessage(), is("User cancelled the authentication consent dialog."));
    }

    @Test
    public void shouldFailWithTextWhenCredentialsRequestFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final FacebookApi.Callback callback = (FacebookApi.Callback) invocation.getArguments()[3];
                callback.onSuccess(createLoginResultFromToken(TOKEN, true));
                return null;
            }
        }).when(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), anyCollectionOf(String.class), any(FacebookApi.Callback.class));
        shouldFailRequest(request);
        when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME))
                .thenReturn(request);

        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        ArgumentCaptor<AuthenticationException> throwableCaptor = ArgumentCaptor.forClass(AuthenticationException.class);
        verify(callback).onFailure(throwableCaptor.capture());
        final AuthenticationException exception = throwableCaptor.getValue();
        assertThat(exception, is(notNullValue()));
    }

    @Test
    public void shouldSucceedIfCredentialsRequestSucceeded() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final FacebookApi.Callback callback = (FacebookApi.Callback) invocation.getArguments()[3];
                callback.onSuccess(createLoginResultFromToken(TOKEN, true));
                return null;
            }
        }).when(apiHelper).login(eq(activity), eq(AUTH_REQ_CODE), anyCollectionOf(String.class), any(FacebookApi.Callback.class));
        shouldYieldCredentialsForRequest(request, credentials);
        when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME))
                .thenReturn(request);

        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);

        ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);
        verify(callback).onSuccess(credentialsCaptor.capture());
        assertThat(credentialsCaptor.getValue(), is(notNullValue()));
        assertThat(credentialsCaptor.getValue(), is(instanceOf(Credentials.class)));
    }

    @Test
    public void shouldLogoutOnClearSession() throws Exception {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.clearSession();
        verify(apiHelper, atLeastOnce()).logout();
    }

    @Test
    public void shouldLogoutOnStop() throws Exception {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.stop();
        verify(apiHelper, atLeastOnce()).logout();
    }

    private LoginResult createLoginResultFromToken(@NonNull String token, boolean allPermissionsGranted) {
        AccessToken accessToken = new AccessToken(token, "appId", "userId", null, null, null, null, null);
        return new LoginResult(accessToken, Collections.<String>emptySet(), allPermissionsGranted ? Collections.<String>emptySet() : Collections.singleton("permission"));
    }

    private void shouldYieldCredentialsForRequest(AuthenticationRequest request, final Credentials credentials) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //noinspection unchecked
                BaseCallback<Credentials, AuthenticationException> callback = (BaseCallback<Credentials, AuthenticationException>) invocation.getArguments()[0];
                callback.onSuccess(credentials);
                return null;
            }
        }).when(request).start(Matchers.<BaseCallback<Credentials, AuthenticationException>>any());
    }

    private void shouldFailRequest(AuthenticationRequest request) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //noinspection unchecked
                BaseCallback<Credentials, AuthenticationException> callback = (BaseCallback<Credentials, AuthenticationException>) invocation.getArguments()[0];
                callback.onFailure(new AuthenticationException("error"));
                return null;
            }
        }).when(request).start(Matchers.<BaseCallback<Credentials, AuthenticationException>>any());
    }
}