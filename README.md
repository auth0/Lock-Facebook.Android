# Lock-Facebook

[![CircleCI](https://img.shields.io/circleci/project/github/auth0/Lock-Facebook.Android.svg?style=flat-square)](https://circleci.com/gh/auth0/Lock-Facebook.Android/tree/master)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](http://doge.mit-license.org)
[![Maven Central](https://img.shields.io/maven-central/v/com.auth0.android/lock-facebook.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.auth0.android%22%20AND%20a%3A%22lock-facebook%22)
[![Download](https://api.bintray.com/packages/auth0/android/lock-facebook/images/download.svg) ](https://bintray.com/auth0/android/lock-facebook/_latestVersion)


[Auth0](https://auth0.com) is an authentication broker that supports social identity providers as well as enterprise identity providers such as Active Directory, LDAP, Google Apps and Salesforce.

Lock-Facebook helps you integrate native Login with [Facebook Android SDK](https://github.com/facebook/facebook-android-sdk) and [Lock](https://auth0.com/lock)

# Deprecation notice

This package relies on a token endpoint that is now considered deprecated. **If your Auth0 client was created after Jun 8th 2017 you won't be able to use this package**. This repository is left for reference purposes.

**We recommend using browser-based flows to authenticate users**. You can do that using the [auth0.android](https://github.com/auth0/auth0.android#authentication-with-hosted-login-page) package's `WebAuthProvider` class, as explained in [this document](https://auth0.com/docs/libraries/auth0-android).

## Requirements

Android 4.0 or later & Facebook Android SDK 4.+

## Install

The Lock-Facebook is available through [Maven Central](http://search.maven.org) and [JCenter](https://bintray.com/bintray/jcenter). To install it, simply add the following line to your `build.gradle`:

```gradle
compile 'com.auth0.android:lock-facebook:3.1.0'
```

### Facebook Developers Console
1. Go to the [Facebook Developers Console](https://developers.facebook.com/) and add a new App: Choose "Android" and give it a valid name. Click "Create new Facebook App ID".
2. Follow the Android Quickstart provided by Facebook. When you're done, you'll end up in your Application's screen. Take note of the `APP ID` and `APP SECRET` values.
3. On the left side you have the navigation drawer. Click Settings and then Basic. Turn ON the **Single Sign On** switch and click the Save button.
4. Now click Settings and then Advanced. Turn ON the **Native or desktop app?** switch.

### Auth0 Dashboard
1. Go to the Auth0 Dashboard and click [Social Connections](https://manage.auth0.com/#/connections/social). Click **Facebook** and a dialog will prompt.
2. Complete the "App ID" field with the `APP ID` value obtained in the step 2 of the **Facebook Developers Console** section above.
3. Complete the "App Secret" field with the `APP SECRET` value obtained in the step 2 of the **Facebook Developers Console** section above. Click the Save button.
4. Go to the Auth0 Dashboard and click [Clients](https://manage.auth0.com/#/clients). If you haven't created yet one, do that first and get into your client configuration page. At the bottom of the page, click the "Show Advanced Settings" link and go to the "Mobile Settings" tab.
5. In the Android section, complete the **Package Name** with your application's package name. Finally, complete the **Key Hashes** field with the SHA-256 of the certificate you're using to sign your application.

### Android Application
1. In your android application, create a new String resource in the `res/strings.xml` file. Name it `facebook_app_id` and set as value the `APP ID` obtained in the step 2 of the **Facebook Developers Console** setup section above.
2. Add the `FacebookActivity` and `facebook_app_id` MetaData to the `AndroidManifest.xml` file, inside the Application tag.

```xml
<activity
    android:name="com.facebook.FacebookActivity"
    android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
    android:label="@string/app_name"/>
<meta-data
    android:name="com.facebook.sdk.ApplicationId"
    android:value="@string/facebook_app_id" />
```

3. Add the Internet Android permission to your `AndroidManifest.xml` file.

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

4. Create a new instance of the `FacebookAuthProvider`.

```java
public class MainActivity extends AppCompatActivity {
  private FacebookAuthProvider provider;
  // ...

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Auth0 auth0 = new Auth0(getString(R.string.com_auth0_client_id), getString(R.string.com_auth0_domain));
    final AuthenticationAPIClient client = new AuthenticationAPIClient(auth0);
    provider = new FacebookAuthProvider(client);
  }

  // ...
}
```

Depending on your use case, you'll need to add a few more lines of code to capture the authorization result. Follow the guides below:

> If you need further help with the setup, please check Facebook's [Getting Started Guide](https://developers.facebook.com/docs/android/getting-started).


## Authenticate with Lock

This library includes an implementation of the `AuthHandler` interface for you to use it directly with **Lock**. Create a new instance of the `FacebookAuthHandler` class passing a valid `FacebookAuthProvider`. Don't forget to customize the permissions if you need to.

```java
Auth0 auth0 = new Auth0("auth0-client-id", "auth0-domain");

FacebookAuthProvider provider = new FacebookAuthProvider(new AuthenticationAPIClient(auth0));
provider.setPermissions(Arrays.asList("public_profile", "user_photos"));

FacebookAuthHandler handler = new FacebookAuthHandler(provider);
```

Finally in the Lock Builder, call `withAuthHandlers` passing the recently created instance.

```java
lock = Lock.newBuilder(auth0, authCallback)
        .withAuthHandlers(handler)
        //...
        .build(this);
```

That's it! When **Lock** needs to authenticate using that connection name, it will ask the `FacebookAuthHandler` for a valid `AuthProvider`.

> We provide this demo in the `PhotosActivity` class. We also use the Facebook SDK to get the User Albums and show them on a list.

## Authenticate without Lock

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

Call `start` to begin the authentication flow.

```java
provider.start(this, callback, RC_PERMISSIONS, RC_AUTHENTICATION);
```

That's it! You'll receive the result in the `AuthCallback` you passed.

> We provide this demo in the `SimpleActivity` class.


## Additional options

### Using a custom connection name
To use a custom social connection name to authorize against Auth0, call `setConnection` with your new connection name.

```java
FacebookAuthProvider provider = new FacebookAuthProvider("my_connection_name", client);
```

### Requesting custom Facebook Permissions
By default, the permission `public_profile` is requested. You can customize them by calling `setPermissions` with the list of Permissions.

```java
provider.setPermissions(Arrays.asList("public_profile", "user_photos"));
```

### Requesting custom Android Runtime Permissions
This provider doesn't require any special Android Manifest Permission to authenticate the user. But if your use case requires them, you can let the AuthProvider handle them for you. Use the `setRequiredPermissions` method.

```java
provider.setRequiredPermissions(new String[]{"android.permission.GET_ACCOUNTS"});
```

### Log out / Clear account.
To log out the user so that the next time he's prompted to input his credentials call `clearSession`. After you do this the provider state will be invalid and you will need to call `start` again before trying to `authorize` a result. Calling `stop` has the same effect.

```java
provider.clearSession();
```

### Remember the Last Login
By default, this provider will remember the last account used to log in. If you want to change this behavior, use the following method.

```java
provider.rememberLastLogin(false);
```

## Issue Reporting

If you have found a bug or if you have a feature request, please report them at this repository issues section. Please do not report security vulnerabilities on the public GitHub issue tracker. The [Responsible Disclosure Program](https://auth0.com/whitehat) details the procedure for disclosing security issues.

## What is Auth0?

Auth0 helps you to:

* Add authentication with [multiple authentication sources](https://docs.auth0.com/identityproviders), either social like **Google, Facebook, Microsoft Account, LinkedIn, GitHub, Twitter, Box, Salesforce, among others**, or enterprise identity systems like **Windows Azure AD, Google Apps, Active Directory, ADFS or any SAML Identity Provider**.
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
