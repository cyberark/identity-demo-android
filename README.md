# CyberArk Identity SDK Sample Application - Kotlin Android

This sample application demonstrates the integration of [CyberArk Identity SDK] into a Kotlin Android application.

## Use Cases

- Login (Authenticates user)
1. Launch authorize URL in the browser Chrome Custom Tabs using OAuth PKCE flow
2. Open CyberArk Identity login web page
3. Authenticates user in browser
4. After the user authenticates, they are redirected back to the application and exchanging the received authorization code for access token and/or refresh token
5. Save the access token and/or refresh token in device storge using Keystore encryption

- Logout (Logout user)
1. Launch end session URL in the browser Chrome Custom Tabs
2. Clear access token from browser cookie
3. End session

- Enroll (Enroll devices using access token)

- QR Code Authenticator (Authenticates web app using QR Code Authenticator from mobile app)
1. Reuest for camera permission
2. Open QR Code scanner
3. Scan web page QR widget from mobile app
4. Client SDK will make REST API call using scanned data and access token
5. CyberArk Server will verify and authenticates the web app

- Biometrics (Enable/disable the biometric options from the settings)
1. Invoke strong biometrics on app launch
2. Invoke strong biometrics when access token expires

## Prerequisites

Before running this sample, you will need the following settings from the Admin dashboard
* Sign up for a user at https://cyberark.my.idaptive.app/ (CyberArk Developer Account)
* Add a new OAuth Application and configured for a user/role

Settings:
* Application ID - Specify the name or "target" that the mobile application uses to find this application

General Usage:
* Client ID Type - Select List
* Allowed Clients - Add Client ID, e.g. add app package name as client ID
* Allowed Redirects - Add Mobile app callback URL, e.g. {scheme}://{host}/android/{applicationId}/callback

Token:
* Token Type - JwtRS256
* Auth methods - Auth Code
* Issue refresh tokens - Checked

Scope:
* Name - All
* Allowed REST APIs - .*

Permissions:
* Add a User or Role

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
