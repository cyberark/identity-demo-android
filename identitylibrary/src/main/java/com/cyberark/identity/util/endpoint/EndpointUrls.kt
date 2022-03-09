/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyberark.identity.util.endpoint

/**
 * All Endpoint urls
 *
 */
internal object EndpointUrls {

    const val URL_FAST_ENROLL_V3 = "/Device/EnrollAndroidDevice"
    const val URL_UPDATE_DEV_SETTINGS = "/IosAppRest/UpdateDevSettings"
    const val URL_SUBMIT_OTP_CODE = "/IosAppRest/SubmitOtpCode"
    const val URL_SIGNUP_USER = "/User/Signup"

    const val HEADER_X_IDAP_NATIVE_CLIENT = "X-IDAP-NATIVE-CLIENT"
    const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
    const val HEADER_AUTHORIZATION = "Authorization"

    const val QUERY_OTP_CODE = "otpCode"
    const val QUERY_OTP_KEY_VERSION = "optKeyVersion"
    const val QUERY_OTP_TIMESTAMP = "otpTimestamp"
    const val QUERY_USER_ACCEPTED = "userAccepted"
    const val QUERY_OTP_CODE_EXPIRY_INTERVAL = "otpCodeExpiryInterval"
    const val QUERY_OTP_CHALLENGE_ANSWER = "challengeAnswer"
    const val QUERY_OTP_OATH_PROFILE_UUID = "oathProfileUuid"
}