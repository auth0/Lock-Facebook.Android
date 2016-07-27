package com.auth0.android.facebook;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.mockito.internal.verification.VerificationModeFactory;

import java.util.Arrays;

import edu.emory.mathcs.backport.java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class FacebookAuthProviderTest {

    private static final int AUTH_REQ_CODE = 123;
    private static final int PERMISSION_REQ_CODE = 122;
    private static final String CONNECTION_NAME = "facebook";
    private static final String TOKEN = "a.raNDom.TokeN";

    @Mock
    AuthenticationAPIClient client;
    @Mock
    AuthCallback callback;
    @Mock
    Activity activity;

    FacebookAuthProviderMock provider;
    @Mock
    FacebookApiHelper apiHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        provider = new FacebookAuthProviderMock(client, apiHelper);
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
        provider.setConnection("my-custom-connection");

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

        Mockito.verify(apiHelper).login(activity, AUTH_REQ_CODE, provider.getPermissions());
    }

    @Test
    public void shouldParseAuthorization() {
        Intent intent = Mockito.mock(Intent.class);
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.authorize(AUTH_REQ_CODE, Activity.RESULT_OK, intent);
        provider.authorize(AUTH_REQ_CODE, Activity.RESULT_CANCELED, intent);

        Mockito.verify(apiHelper).finishLogin(AUTH_REQ_CODE, Activity.RESULT_OK, intent);
        Mockito.verify(apiHelper).finishLogin(AUTH_REQ_CODE, Activity.RESULT_CANCELED, intent);
    }

    @Test
    public void shouldReturnDelegatedParseResult() {
        Intent intent = Mockito.mock(Intent.class);

        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        Mockito.when(apiHelper.finishLogin(AUTH_REQ_CODE, Activity.RESULT_OK, intent)).thenReturn(true);
        Mockito.when(apiHelper.finishLogin(999, Activity.RESULT_OK, intent)).thenReturn(false);

        assertThat(provider.authorize(AUTH_REQ_CODE, Activity.RESULT_OK, intent), is(true));
        assertThat(provider.authorize(999, Activity.RESULT_OK, intent), is(false));
    }

    @Test
    public void shouldDoNothingWhenCalledFromNewIntent() {
        assertThat(provider.authorize(null), is(false));
    }

    @Test
    public void shouldNotRequireAndroidPermissions() {
        assertThat(provider.getRequiredAndroidPermissions(), is(notNullValue()));
        assertThat(provider.getRequiredAndroidPermissions(), is(IsArrayWithSize.<String>emptyArray()));
    }

    @Test
    public void shouldNotCallAuth0OAuthEndpointWhenSomePermissionsWereRejected() {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        final AuthenticationRequest request = Mockito.mock(AuthenticationRequest.class);
        Mockito.when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME)).thenReturn(request);
        provider.facebookCallback.onSuccess(createLoginResultFromToken(TOKEN, false));

        Mockito.verify(client, VerificationModeFactory.noMoreInteractions()).loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME);
    }

    @Test
    public void shouldFailWithTextWhenSomePermissionsWereRejected() {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        final AuthenticationRequest request = Mockito.mock(AuthenticationRequest.class);
        Mockito.when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME)).thenReturn(request);
        provider.facebookCallback.onSuccess(createLoginResultFromToken(TOKEN, false));

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        ArgumentCaptor<Integer> titleResCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> messageResCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(callback).onFailure(titleResCaptor.capture(), messageResCaptor.capture(), throwableCaptor.capture());
        assertThat(throwableCaptor.getValue(), is(nullValue()));
        assertThat(titleResCaptor.getValue(), is(R.string.com_auth0_facebook_authentication_failed_title));
        assertThat(messageResCaptor.getValue(), is(R.string.com_auth0_facebook_authentication_failed_missing_permissions_message));
    }

    @Test
    public void shouldCallAuth0OAuthEndpointWhenFacebookTokenIsReceived() {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        final AuthenticationRequest request = Mockito.mock(AuthenticationRequest.class);
        Mockito.when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME)).thenReturn(request);
        provider.facebookCallback.onSuccess(createLoginResultFromToken(TOKEN, true));

        Mockito.verify(client).loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME);
    }

    @Test
    public void shouldCallAuth0OAuthEndpointWithCustomConnectionNameWhenGoogleTokenIsReceived() {
        provider.setConnection("my-custom-connection");
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        final AuthenticationRequest request = Mockito.mock(AuthenticationRequest.class);
        Mockito.when(client.loginWithOAuthAccessToken(TOKEN, "my-custom-connection")).thenReturn(request);
        provider.facebookCallback.onSuccess(createLoginResultFromToken(TOKEN, true));

        Mockito.verify(client).loginWithOAuthAccessToken(TOKEN, "my-custom-connection");
    }

    @Test
    public void shouldFailWithTextWhenFacebookRequestFailed() {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.facebookCallback.onError(new FacebookException("error"));

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        ArgumentCaptor<Integer> titleResCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> messageResCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(callback).onFailure(titleResCaptor.capture(), messageResCaptor.capture(), throwableCaptor.capture());
        assertThat(throwableCaptor.getValue(), is(instanceOf(FacebookException.class)));
        assertThat(titleResCaptor.getValue(), is(R.string.com_auth0_facebook_authentication_failed_title));
        assertThat(messageResCaptor.getValue(), is(R.string.com_auth0_facebook_authentication_failed_message));
    }

    @Test
    public void shouldFailWithTextWhenFacebookRequestIsCancelled() {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.facebookCallback.onCancel();

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        ArgumentCaptor<Integer> titleResCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> messageResCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(callback).onFailure(titleResCaptor.capture(), messageResCaptor.capture(), throwableCaptor.capture());
        assertThat(throwableCaptor.getValue(), is(nullValue()));
        assertThat(titleResCaptor.getValue(), is(R.string.com_auth0_facebook_authentication_failed_title));
        assertThat(messageResCaptor.getValue(), is(R.string.com_auth0_facebook_authentication_cancelled_error_message));
    }

    @Test
    public void shouldFailWithTextWhenCredentialsRequestFailed() {
        final AuthenticationRequest authRequest = new AuthenticationRequestMock(false);
        Mockito.when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME))
                .thenReturn(authRequest);

        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.facebookCallback.onSuccess(createLoginResultFromToken(TOKEN, true));

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        ArgumentCaptor<Integer> titleResCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> messageResCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(callback).onFailure(titleResCaptor.capture(), messageResCaptor.capture(), throwableCaptor.capture());
        assertThat(throwableCaptor.getValue(), is(instanceOf(AuthenticationException.class)));
        assertThat(titleResCaptor.getValue(), is(R.string.com_auth0_facebook_authentication_failed_title));
        assertThat(messageResCaptor.getValue(), is(R.string.com_auth0_facebook_authentication_failed_message));
    }

    @Test
    public void shouldSucceedIfCredentialsRequestSucceeded() {
        final AuthenticationRequest authRequest = new AuthenticationRequestMock(true);
        Mockito.when(client.loginWithOAuthAccessToken(TOKEN, CONNECTION_NAME))
                .thenReturn(authRequest);

        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.facebookCallback.onSuccess(createLoginResultFromToken(TOKEN, true));

        ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);
        Mockito.verify(callback).onSuccess(credentialsCaptor.capture());
        assertThat(credentialsCaptor.getValue(), is(notNullValue()));
        assertThat(credentialsCaptor.getValue(), is(instanceOf(Credentials.class)));
    }

    @Test
    public void shouldLogoutOnClearSession() {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.clearSession();
        Mockito.verify(apiHelper).logout();
    }

    @Test
    public void shouldLogoutOnStop() {
        provider.start(activity, callback, PERMISSION_REQ_CODE, AUTH_REQ_CODE);
        provider.stop();
        Mockito.verify(apiHelper).logout();
    }

    private LoginResult createLoginResultFromToken(@NonNull String token, boolean allPermissionsGranted) {
        AccessToken accessToken = new AccessToken(token, "appId", "userId", null, null, null, null, null);
        return new LoginResult(accessToken, Collections.emptySet(), allPermissionsGranted ? Collections.emptySet() : Sets.newSet("permission"));
    }

}