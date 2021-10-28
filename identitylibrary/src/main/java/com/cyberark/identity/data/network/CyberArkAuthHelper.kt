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
import okhttp3.RequestBody

/**
 * CyberArk auth helper is the base class to expose all methods to ViewModel
 *
 * @property cyberArkAuthService: CyberArkAuthService instance
 */
class CyberArkAuthHelper(private val cyberArkAuthService: CyberArkAuthService) {

    /**
     * QR code authenticator
     *
     * @param idapNativeClient: X-IDAP-NATIVE-CLIENT
     * @param bearerToken: Authorization Bearer Token
     * @param url: QR Code login URL
     * @return QRCodeLoginModel
     */
    suspend fun qrCodeLogin(
        idapNativeClient: Boolean,
        bearerToken: String,
        url: String
    ): QRCodeLoginModel = cyberArkAuthService.qrCodeLogin(
        idapNativeClient,
        bearerToken,
        url
    )

    /**
     * Get access token
     *
     * @param params: request body
     * @param url: token Url
     * @return AuthCodeFlowModel
     */
    suspend fun getAccessToken(
        params: HashMap<String?,
                String?>, url: String
    ): AuthCodeFlowModel = cyberArkAuthService.getAccessToken(
        params,
        url
    )

    /**
     * Get refresh token
     *
     * @param params: request body
     * @param url: token Url
     * @return RefreshTokenModel
     */
    suspend fun refreshToken(
        params: HashMap<String?,
                String?>, url: String
    ): RefreshTokenModel = cyberArkAuthService.refreshToken(params, url)

    /**
     * Enroll device
     * @param idapNativeClient: idap native client
     * @param acceptLang: accepted language
     * @param bearerToken: authorization bearer token
     * @param body: request body
     * @return EnrollmentModel
     */
    suspend fun fastEnrollV3(
        idapNativeClient: Boolean,
        acceptLang: String,
        bearerToken: String,
        body: RequestBody
    ): EnrollmentModel = cyberArkAuthService.fastEnrollV3(
        idapNativeClient,
        acceptLang,
        bearerToken,
        body
    )

    /**
     * Send FCM token
     *
     * @param idapNativeClient: idaptive native client
     * @param bearerToken: authorization bearer token
     * @param body: request body
     * @return SendFCMTokenModel
     */
    suspend fun sendFCMToken(
        idapNativeClient: Boolean,
        bearerToken: String,
        body: RequestBody
    ): SendFCMTokenModel = cyberArkAuthService.sendFCMToken(
        idapNativeClient,
        bearerToken,
        body
    )

    /**
     * OTP enroll
     *
     * @param bearerToken: authorization bearer token
     * @param url: OTP enroll URL
     * @return OTPEnrollModel
     */
    suspend fun otpEnroll(
        bearerToken: String,
        url: String
    ): OTPEnrollModel = cyberArkAuthService.otpEnroll(
        bearerToken,
        url
    )

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
     * @return SubmitOTPModel
     */
    suspend fun submitOTPCode(
        bearerToken: String,
        otpCode: String,
        otpKeyVersion: Int,
        otpTimeStamp: Long,
        userAccepted: Boolean,
        otpExpiryInterval: Int,
        otpChallengeAnswer: String,
        udid: String
    ): SubmitOTPModel = cyberArkAuthService.submitOTPCode(
        bearerToken,
        otpCode,
        otpKeyVersion,
        otpTimeStamp,
        userAccepted,
        otpExpiryInterval,
        otpChallengeAnswer,
        udid
    )
}