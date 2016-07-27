# Lock-Facebook

[![Build Status](https://travis-ci.org/auth0/Lock-Facebook.Android.svg?branch=master)](https://travis-ci.org/auth0/Lock-Facebook.Android)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](http://doge.mit-license.org)
[![Maven Central](https://img.shields.io/maven-central/v/com.auth0.android/lock-facebook.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.auth0.android%22%20AND%20a%3A%22lock-facebook%22)
[![Bintray](https://api.bintray.com/packages/auth0/lock-android/lock-facebook/images/download.svg) ](https://bintray.com/auth0/lock-android/lock-facebook/_latestVersion)

[Auth0](https://auth0.com) is an authentication broker that supports social identity providers as well as enterprise identity providers such as Active Directory, LDAP, Google Apps and Salesforce.

Lock-Facebook helps you integrate native Login with [Facebook Android SDK](https://github.com/facebook/facebook-android-sdk) and [Lock](https://auth0.com/lock)

## Requirements

Android 4.0 or later.

## Install

The Lock-Facebook is available through [Maven Central](http://search.maven.org) and [JCenter](https://bintray.com/bintray/jcenter). To install it, simply add the following line to your `build.gradle`:

```gradle
compile 'com.auth0.android:lock-facebook:2.4.+'
```

Then in your project's `AndroidManifest.xml` add the following entries:

```xml
<activity android:name="com.facebook.FacebookActivity"
          android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
          android:theme="@android:style/Theme.Translucent.NoTitleBar"
          android:label="@string/app_name" />
<meta-data android:name="com.facebook.sdk.ApplicationId" android:value="@string/facebook_app_id"/>
```

The value `@string/facebook_app_id` is your Facebook Application ID that you can get from [Facebook Dev Site](https://developers.facebook.com/apps) after you create your Facebook Application. Then just add this value in your `strings.xml` like this:

```xml
<string name="facebook_app_id">YOUR_FB_APP_ID_GOES_HERE</string>
```

> For more information please check [Facebook Getting Started Guide](https://developers.facebook.com/docs/android/getting-started).


### With Lock

Create a new class and make it implement `AuthProviderResolver`. On the `onAuthProviderRequest` method compare the `connectionName` value against the connection you would like this provider to handle, and if it's a match return a new `FacebookAuthProvider` instance with an `AuthenticationAPIClient`.

```java
public class AuthHandler implements AuthProviderResolver {

    @Nullable
    @Override
    public AuthProvider onAuthProviderRequest(Context context, @NonNull AuthCallback callback, @NonNull String connectionName) {
        AuthProvider provider = null;
        if (connectionName.equals("facebook")) {
            Auth0 auth0 = new Auth0("auth0-client-id", "auth0-domain");
            final AuthenticationAPIClient client = new AuthenticationAPIClient(auth0);
            provider = new FacebookAuthProvider(client);
        }
        return provider;
    }
}

```

Make a new instance of your provider resolver and set it when building the Lock instance.

```java
final AuthHandler authHandler = new AuthHandler(); 
final Lock.Builder builder = Lock.newBuilder(getAccount(), callback);
Lock lock = builder.withProviderResolver(authHandler);
                //...
                .build();
```

That's it! When **Lock** needs to authenticate using that connection name, it will ask the `AuthProviderResolver` for a valid `AuthProvider`.

> We provide this demo in the `PhotosActivity` class. We also use the Facebook SDK to get the User Albums and show them on a list.

### Without Lock

Just create a new instance of `FacebookAuthProvider` with an `AuthenticationAPIClient`.

```java
Auth0 auth0 = new Auth0("auth0-client-id", "auth0-domain");
final AuthenticationAPIClient client = new AuthenticationAPIClient(auth0);
FacebookAuthProvider provider = new FacebookAuthProvider(client);
```

Override your activity's `onActivityResult` method and redirect the received parameters to the provider instance's `authorize` method.

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (provider.authorize(requestCode, resultCode, data)) {
        return;
    }
    super.onActivityResult(requestCode, resultCode, data);
}
```

Call `start` to begin the authentication flow. The permissions request code is ignored as this provider doesn't need any custom android permissions.

```java
provider.start(this, callback, RC_PERMISSIONS, RC_AUTHENTICATION);
```

That's it! You'll receive the result in the `AuthCallback` you passed.

> We provide this demo in the `SimpleActivity` class.

## Using a custom connection name
To use a custom social connection name to authorize against Auth0, call `setConnection` with your new connection name.

```java
provider.setConnection("my-connection")
```

## Log out / Clear account.
To log out the user so that the next time he's prompted to input his credentials call `clearSession`. After you do this the provider state will be invalid and you will need to call `start` again before trying to `authorize` a result.

```java
provider.clearSession();
```
 
> Calling `stop` has the same effect.

## Issue Reporting

If you have found a bug or if you have a feature request, please report them at this repository issues section. Please do not report security vulnerabilities on the public GitHub issue tracker. The [Responsible Disclosure Program](https://auth0.com/whitehat) details the procedure for disclosing security issues.

## What is Auth0?

Auth0 helps you to:

* Add authentication with [multiple authentication sources](https://docs.auth0.com/identityproviders), either social like **Google, Facebook, Microsoft Account, LinkedIn, GitHub, Twitter, Box, Salesforce, amont others**, or enterprise identity systems like **Windows Azure AD, Google Apps, Active Directory, ADFS or any SAML Identity Provider**.
* Add authentication through more traditional **[username/password databases](https://docs.auth0.com/mysql-connection-tutorial)**.
* Add support for **[linking different user accounts](https://docs.auth0.com/link-accounts)** with the same user.
* Support for generating signed [Json Web Tokens](https://docs.auth0.com/jwt) to call your APIs and **flow the user identity** securely.
* Analytics of how, when and where users are logging in.
* Pull data from other sources and add it to the user profile, through [JavaScript rules](https://docs.auth0.com/rules).

## Create a free account in Auth0

1. Go to [Auth0](https://auth0.com) and click Sign Up.
2. Use Google, GitHub or Microsoft Account to login.

## Author

[Auth0](auth0.com)

## License

Lock-Facebook is available under the MIT license. See the [LICENSE](LICENSE) file for more info.
