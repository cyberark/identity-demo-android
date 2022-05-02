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

package com.cyberark.identity.data.network

import com.cyberark.identity.data.model.*
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_FAST_ENROLL_V3
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_SIGNUP_USER
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_SUBMIT_OTP_CODE
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_UPDATE_DEV_SETTINGS
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * CyberArk auth service interface is used to define all endpoint urls
 *
 */
interface CyberArkAuthService {

    /**
     * Qr code login
     *
     * @param idapNativeClient: Idaptive native client
     * @param bearerToken: Authorization Bearer Token
     * @param url: QR Code login URL
     * @return QRCodeLoginModel
     */
    @POST
    suspend fun qrCodeLogin(
        @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
        @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
        @Url url: String
    ): QRCodeLoginModel

    /**
     * Get access token
     *
     * @param params: request body
     * @param url: token Url
     * @return AuthCodeFlowModel
     */
    @FormUrlEncoded
    @POST
    suspend fun getAccessToken(
        @FieldMap params: HashMap<String?, String?>,
        @Url url: String
    ): AuthCodeFlowModel

    /**
     * Get refresh token
     *
     * @param params: request body
     * @param url: token Url
     * @return RefreshTokenModel
     */
    @FormUrlEncoded
    @POST
    suspend fun refreshToken(
        @FieldMap params: HashMap<String?, String?>,
        @Url url: String
    ): RefreshTokenModel

    /**
     * Get user information
     *
     * @param idapNativeClient: idaptive native client
     * @param bearerToken: authorization bearer token
     * @param url: token Url
     * @return UserInfoModel
     */
    @POST
    suspend fun getUserInfo(
        @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
        @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
        @Url url: String
    ): UserInfoModel

    /**
     * Enroll device
     *
     * @param idapNativeClient: idaptive native client
     * @param acceptLang: accepted language
     * @param bearerToken: authorization bearer token
     * @param payload: request body
     * @return EnrollmentModel
     */
    @POST(URL_FAST_ENROLL_V3)
    suspend fun fastEnrollV3(
        @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
        @Header(EndpointUrls.HEADER_ACCEPT_LANGUAGE) acceptLang: String,
        @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
        @Body payload: RequestBody
    ): EnrollmentModel

    /**
     * Send FCM token
     *
     * @param idapNativeClient: idaptive native client
     * @param bearerToken: authorization bearer token
     * @param payload: request body
     * @return SendFCMTokenModel
     */
    @POST(URL_UPDATE_DEV_SETTINGS)
    suspend fun sendFCMToken(
        @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
        @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
        @Body payload: RequestBody
    ): SendFCMTokenModel

    /**
     * OTP enroll
     *
     * @param bearerToken: authorization bearer token
     * @param url: OTP enroll URL
     * @return OTPEnrollModel
     */
    @POST
    suspend fun otpEnroll(
        @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
        @Url url: String
    ): OTPEnrollModel

    /**
     * Submit OTP code
     *
     * @param bearerToken: authorization bearer token
     * @param otpCode: OTP Code
     * @param otpKeyVersion: OTP Key Version
     * @param otpTimeStamp: OTP Timestamp
     * @param userAccepted: user accepted status
     * @param otpExpiryInterval: OTP expiry interval
     * @param otpChallengeAnswer: OTP challenge answer
     * @param udid: profile UDID
     * @return
     */
    @POST(URL_SUBMIT_OTP_CODE)
    suspend fun submitOTPCode(
        @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
        @Query(EndpointUrls.QUERY_OTP_CODE) otpCode: String,
        @Query(EndpointUrls.QUERY_OTP_KEY_VERSION) otpKeyVersion: Int,
        @Query(EndpointUrls.QUERY_OTP_TIMESTAMP) otpTimeStamp: Long,
        @Query(EndpointUrls.QUERY_USER_ACCEPTED) userAccepted: Boolean,
        @Query(EndpointUrls.QUERY_OTP_CODE_EXPIRY_INTERVAL) otpExpiryInterval: Int,
        @Query(EndpointUrls.QUERY_OTP_CHALLENGE_ANSWER) otpChallengeAnswer: String,
        @Query(EndpointUrls.QUERY_OTP_OATH_PROFILE_UUID) udid: String,
    ): SubmitOTPModel

    /**
     * Signup with captcha
     *
     * @param idapNativeClient: idaptive native client
     * @param payload: request body
     * @return SignupCaptchaModel
     */
    @POST(URL_SIGNUP_USER)
    suspend fun signupWithCaptcha(
        @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
        @Body payload: RequestBody
    ): SignupCaptchaModel
}