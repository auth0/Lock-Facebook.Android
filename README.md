# Lock-Facebook

[![Build Status](https://travis-ci.org/auth0/Lock-Facebook.Android.svg?branch=master)](https://travis-ci.org/auth0/Lock-Facebook.Android)
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat)](http://doge.mit-license.org)
[![Maven Central](https://img.shields.io/maven-central/v/com.auth0.android/lock-facebook.svg)](http://search.maven.org/#browse%7C1197001692)
[![Download](https://api.bintray.com/packages/auth0/lock-android/lock-facebook/images/download.svg) ](https://bintray.com/auth0/lock-android/lock-facebook/_latestVersion)

[Auth0](https://auth0.com) is an authentication broker that supports social identity providers as well as enterprise identity providers such as Active Directory, LDAP, Google Apps and Salesforce.

Lock-Facebook helps you integrate native Login with [Facebook Android SDK](https://github.com/facebook/facebook-android-sdk) and [Lock](https://auth0.com/lock)

## Requierements

Android 4.0 or later.

## Install

The Lock-Facebook is available through [Maven Central](http://search.maven.org) and [JCenter](https://bintray.com/bintray/jcenter). To install it, simply add the following line to your `build.gradle`:

```gradle
compile 'com.auth0.android:lock-facebook:2.0.+'
```

Then in your project's `AndroidManifest.xml` add the following entries:

```xml
<activity android:name="com.facebook.LoginActivity"/>
<meta-data android:name="com.facebook.sdk.ApplicationId" android:value="@string/facebook_app_id"/>
```

The value `@string/facebook_app_id` is your Facebook Application ID that you can get from [Facebook Dev Site](https://developers.facebook.com/apps) after you create your Facebook Application. Then just add this value in your `strings.xml` like this:

```xml
<string name="facebook_app_id">YOUR_FB_APP_ID_GOES_HERE</string>
```

> For more information please check [Facebook Getting Started Guide](https://developers.facebook.com/docs/android/getting-started).

## Usage

Just create a new instance of `FacebookIdentityProvider`

```java
    FacebookIdentityProvider facebook = new FacebookIdentityProvider();
```

and register it with your instance of `Lock`

```java
Lock lock = ...;
lock.setProvider(Strategies.Facebook.getName(), facebook);
```

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

Auth0

## License

Lock-Facebook is available under the MIT license. See the [LICENSE file](LICENSE) for more info.
