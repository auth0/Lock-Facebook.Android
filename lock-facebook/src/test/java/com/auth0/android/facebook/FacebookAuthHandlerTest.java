package com.auth0.android.facebook;

import com.auth0.android.provider.AuthProvider;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class FacebookAuthHandlerTest {

    @Mock
    private FacebookAuthProvider provider;

    private FacebookAuthHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new FacebookAuthHandler(provider);
    }

    @Test
    public void shouldGetFacebookProvider() throws Exception {
        assertThat(handler.providerFor("facebook", "facebook"), Is.<AuthProvider>is(provider));
    }

    @Test
    public void shouldGetFacebookProviderFromConnectionWithoutDefaultValue() throws Exception {
        assertThat(handler.providerFor("facebook", "facebook-2"), Is.<AuthProvider>is(provider));
    }

    @Test
    public void shouldGetNullProvider() throws Exception {
        assertThat(handler.providerFor("some-strategy", "some-connection"), is(nullValue()));
    }
}