package com.auth0.android.facebook.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.facebook.FacebookAuthHandler;
import com.auth0.android.facebook.FacebookAuthProvider;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PhotosActivity extends AppCompatActivity {

    private static final String TAG = PhotosActivity.class.getSimpleName();
    private static final String FACEBOOK_CONNECTION = "facebook";

    private Lock lock;
    private PhotosAdapter adapter;
    private List<String> photos;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        FacebookAuthProvider provider = new FacebookAuthProvider(new AuthenticationAPIClient(getAccount()));
        provider.setPermissions(Arrays.asList("public_profile", "user_photos"));
        lock = Lock.newBuilder(getAccount(), authCallback)
                .withAuthHandlers(new FacebookAuthHandler(provider))
                .allowedConnections(Collections.singletonList(FACEBOOK_CONNECTION))
                .closable(true)
                .build(this);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(lock.newIntent(PhotosActivity.this));
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        photos = new ArrayList<>();
        adapter = new PhotosAdapter(this, photos);
        GridView gridView = (GridView) findViewById(R.id.grid);
        gridView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lock.onDestroy(this);
        lock = null;
    }

    private Auth0 getAccount() {
        return new Auth0(getString(R.string.com_auth0_client_id), getString(R.string.com_auth0_domain));
    }

    private void fetchAlbums() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            final List<String> urls = parseAlbumData(object.getJSONObject("albums").getJSONArray("data"));
                            if (urls.isEmpty()) {
                                Toast.makeText(PhotosActivity.this, "You have no albums on Facebook!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            photos.clear();
                            photos.addAll(urls);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,albums.limit(100){name,picture{url}}");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private List<String> parseAlbumData(JSONArray albumData) {
        List<String> covers = new ArrayList<>();
        for (int i = 0; i < albumData.length(); i++) {
            try {
                covers.add(albumData
                        .getJSONObject(i)
                        .getJSONObject("picture")
                        .getJSONObject("data")
                        .getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return covers;
    }

    private LockCallback authCallback = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {
            Log.i(TAG, "Auth ok! User has given us all facebook requested permissions.");
            progressBar.setVisibility(View.VISIBLE);
            fetchAlbums();
        }

        @Override
        public void onCanceled() {
            Toast.makeText(PhotosActivity.this, "Authentication cancelled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(LockException error) {
            Toast.makeText(PhotosActivity.this, "Error occurred. Please retry.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error occurred: " + error.getMessage());
        }
    };
}
