package com.auth0.android.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FacebookSignInDelegateActivity extends Activity {

    private static final String FACEBOOK_PERMISSIONS_EXTRA = "com.auth0.android.facebook.FACEBOOK_PERMISSIONS";
    private static final String PROVIDER_REQUEST_CODE_EXTRA = "com.auth0.android.facebook.PROVIDER_REQUEST_CODE";

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

    static void signIn(@NonNull Activity activity, int requestCode, @NonNull Collection<String> permissions) {
        Intent delegateIntent = new Intent(activity, FacebookSignInDelegateActivity.class);
        delegateIntent.putStringArrayListExtra(FACEBOOK_PERMISSIONS_EXTRA, new ArrayList<>(permissions));
        activity.startActivityForResult(delegateIntent, requestCode);
    }

    static boolean finishSignIn(@NonNull CallbackManager callbackManager, int requestCode, int resultCode, @NonNull Intent intent) {
        int reqCode = intent.getIntExtra(PROVIDER_REQUEST_CODE_EXTRA, requestCode);
        return callbackManager.onActivityResult(reqCode, resultCode, intent);
    }
}
