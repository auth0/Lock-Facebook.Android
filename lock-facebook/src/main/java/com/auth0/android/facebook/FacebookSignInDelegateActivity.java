package com.auth0.android.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.login.LoginManager;

import java.util.Collections;
import java.util.List;

public class FacebookSignInDelegateActivity extends Activity {

    public static final String FACEBOOK_PERMISSIONS_EXTRA = "com.auth0.android.facebook.FACEBOOK_PERMISSIONS";
    public static final String PROVIDER_REQUEST_CODE_EXTRA = "com.auth0.android.facebook.PROVIDER_REQUEST_CODE";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> permissions = Collections.emptyList();
        if (getIntent().hasExtra(FACEBOOK_PERMISSIONS_EXTRA)) {
            permissions = getIntent().getStringArrayListExtra(FACEBOOK_PERMISSIONS_EXTRA);
        }
        LoginManager.getInstance().logInWithReadPermissions(this, permissions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        data.putExtra(PROVIDER_REQUEST_CODE_EXTRA, requestCode);
        setResult(resultCode, data);
        finish();
    }
}
