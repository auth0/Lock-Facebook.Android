package com.auth0.android.facebook;

import com.auth0.android.provider.AuthProvider;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class FacebookAuthHandlerTest {

    private FacebookAuthProvider provider;
    private FacebookAuthHandler handler;

    @Before
    public void setUp() throws Exception {
        provider = mock(FacebookAuthProvider.class);
        handler = new FacebookAuthHandler(provider);
    }

    @Test
    public void shouldGetFacebookProvider() throws Exception {
        final AuthProvider p = handler.providerFor("facebook", "facebook");
        assertThat(p, is(notNullValue()));
        assertThat(p, is(CoreMatchers.instanceOf(FacebookAuthProvider.class)));
        assertThat(p, is(equalTo((AuthProvider) provider)));
    }

    @Test
    public void shouldGetNullProvider() throws Exception {
        final AuthProvider p = handler.providerFor("some-strategy", "some-connection");
        assertThat(p, is(nullValue()));
    }
}