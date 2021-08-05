# CyberArk Identity SDK Sample Application
**Status**: Community

The Login Widget Demo is available with a Community Certification Level.
Naming and API's are still subject to *breaking* changes.

![Certification Level Community](https://camo.githubusercontent.com/fc39ec5a52592c929ecd6e7ff4e3d1b7d5a4856c512a5486a5c24a00db6bcf6d/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f43657274696669636174696f6e2532304c6576656c2d436f6d6d756e6974792d3238413734353f6c696e6b3d68747470733a2f2f6769746875622e636f6d2f637962657261726b2f636f6d6d756e6974792f626c6f622f6d61737465722f436f6e6a75722f636f6e76656e74696f6e732f63657274696669636174696f6e2d6c6576656c732e6d64)

This sample application demonstrates the integration of CyberArk Identity SDK into a Kotlin Android application.

# Contents
<!-- MarkdownTOC -->
- [Use Cases](#Use-Cases)
- [Prerequisites](#prerequisites)
- [SDK Integration Requirements](#SDK-Integration-Requirements)
- [Project setup](#Project-setup)
	- [Configure CyberArk Identity Account](#Configure-CyberArk-Identity-Account)
	- [Update the Redirect URI Host and Scheme](#Update-the-Redirect-URI-Host-and-Scheme)
	- [Dependencies](#dependencies)
	- [Running the app](#Running-the-app)
- [What is CyberArk Identity?](#What-is-CyberArk-Identity?)
- [Issue Reporting](#Issue-Reporting)
- [Code Maintainers](#Code-Maintainers)
- [License](#license)
<a id="web-app-setup"></a>

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

- QR Code Authenticator (Authenticates web app using QR Code Authenticator from the mobile app)
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

```xml

<resources>
    <string name="cyberark_account_client_id">{clientId}</string>
    <string name="cyberark_account_host">{host}</string>
    <string name="cyberark_account_app_id">{appId}</string>
    <string name="cyberark_account_response_type">code</string>
    <string name="cyberark_account_scope">{scope}</string>
    <string name="cyberark_account_redirect_uri">{scheme}://{host}/android/{applicationId}/callback</string>
    <string name="cyberark_account_scheme">{scheme}</string>
</resources>

```

### Update the Redirect URI Host and Scheme

Include gradle manifest placeholder in your app's `build.gradle`

```bash
manifestPlaceholders = [cyberarkIdentityHost: "@string/cyberark_account_host",
                                cyberarkIdentityScheme: "@string/cyberark_account_scheme"]
```
Make sure this is consistent with the CyberArk Identity Account Info

### Dependencies

Include CyberArk SDK dependency in `build.gradle` file:

```bash
implementation 'com.cyberark.identity:mfa-android:0.0.1' (//TODO.. need to be uploaded in Maven central repo)
```

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
Please follow [SECURITY.md](../SECURITY.md)

## Code Maintainers
[CyberArk Identity Team](https://www.cyberark.com)

<a id="license"></a>
## License
This project is licensed under Apache - see [`LICENSE`](../LICENSE) for more details.
