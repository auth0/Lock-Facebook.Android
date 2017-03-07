package com.auth0.android.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import java.util.Collections;
import java.util.List;

public class FacebookSignInDelegateActivity extends AppCompatActivity {

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
        if (FacebookSdk.isFacebookRequestCode(requestCode)) {
            data.putExtra(PROVIDER_REQUEST_CODE_EXTRA, requestCode);
            setResult(resultCode, data);
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
