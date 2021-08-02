# CyberArk Identity SDK Sample Application - Kotlin Android

This sample application demonstrates the integration of [CyberArk Identity SDK] into a Kotlin Android application.

## Use Cases

- Login
Step 1: Launch authorize URL in the browser Chrome Custom Tabs using OAuth PKCE flow
Step 2: Open CyberArk Identity login web page
Step 3: Authenticates user in browser
Step 4: After the user authenticates, they are redirected back to the application and exchanging the received authorization code for access token and/or refresh token
Step 5: Save the access token and/or refresh token in device storge using Keystore encryption

- Logout
Step 1: Launch end session URL in the browser Chrome Custom Tabs
Step 2: Clear access token from browser cookie
Step 3: End session

- Enroll
Enroll devices using access token

- QR Code Authenticator
Authenticates web app using QR Code Authenticator from mobile app
Step 1: Reuest for camera permission
Step 2: Open QR Code scanner
Step 3: Scan web page QR widget from mobile app
Step 4: Client SDK will make REST API call using scanned data and access token
Step 5: CyberArk Server will verify and authenticates the web app

- Biometrics
Enable/disable the biometric options from the settings
1. Invoke strong biometrics on app launch
2. Invoke strong biometrics when access token expires

## Prerequisites

Before running this sample, you will need the following settings from the Admin dashboard
* Sign up for a user at https://cyberark.my.idaptive.app/ (CyberArk Developer Account)
* Add a new OAuth Application and configured for a user/role

Settings:
1. Application ID - Specify the name or "target" that the mobile application uses to find this application

General Usage:
1. Client ID Type - Select List
2. Allowed Clients - Add Client ID, e.g. add app package name as client ID
3. Allowed Redirects - Add Mobile app callback URL, e.g. {scheme}://{host}/android/{applicationId}/callback

Token:
1. Token Type - JwtRS256
2. Auth methods - Auth Code
3. Issue refresh tokens - Checked

Scope:
Name - All
Allowed REST APIs - .*

Permissions:
Add a User or Role

**Note:** *To receive a **refresh_token**, you must checked the `Issue refresh tokens`.*
**Note:** *As with any CyberArk Identity application, make sure you assign Users or Roles to the application. Otherwise, no one can use it.*

## SDK Integration Requirements

Android API version 23 or later and Java 8+.

## Project setup

### Configure CyberArk Identity Account

<resources>
    <string name="cyberark_account_client_id">{clientId}</string>
    <string name="cyberark_account_host">{host}</string>
    <string name="cyberark_account_app_id">{appId}</string>
    <string name="cyberark_account_response_type">code</string>
    <string name="cyberark_account_scope">{scope}</string>
    <string name="cyberark_account_redirect_uri">{scheme}://{host}/android/{applicationId}/callback</string>
    <string name="cyberark_account_scheme">{scheme}</string>
</resources>


### Update the Redirect URI Host and Scheme

Include gradle manifest placeholder in your app's `build.gradle`

manifestPlaceholders = [cyberarkIdentityHost: "@string/cyberark_account_host",
                                cyberarkIdentityScheme: "@string/cyberark_account_scheme"]

Make sure this is consistent with the CyberArk Identity Account Info

### Dependencies

Include CyberArk SDK dependency in `build.gradle` file:
implementation 'com.cyberark.identity:mfa-android:0.0.1' (//TODO.. need to be uploaded in Maven central repo)

### Running the app

Run the application using Android Studio.
**HomeActivity.kt** - Login
**MFAActivity.kt** - Enroll, QR Code Authenticator, Logout, Invoke biometrics on app launch, Invoke biometrics when access token expires

## What is CyberArk Identity?

* https://www.cyberark.com/products/customer-identity/
* https://www.cyberark.com/what-is/ciam/
* https://www.cyberark.com/products/developer-tools/
* https://www.cyberark.com/what-is/identity-security/

## Issue Reporting

## Author
[CyberArk Identity](https://www.cyberark.com)

## License
