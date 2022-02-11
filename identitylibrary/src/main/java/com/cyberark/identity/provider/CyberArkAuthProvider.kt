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

package com.cyberark.identity.provider

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.*
import com.cyberark.identity.provider.manager.*
import com.cyberark.identity.util.ResponseHandler
import org.json.JSONObject

/**
 * CyberArk auth provider is used to provide API access for multi-factor authentication
 *
 */
object CyberArkAuthProvider {

    private val TAG: String? = CyberArkAuthProvider::class.simpleName
    internal var cyberArkAuthInterface: CyberArkAuthInterface? = null


    fun login(account: CyberArkAccountBuilder): LoginBuilder {
        return LoginBuilder(account)
    }

    fun endSession(account: CyberArkAccountBuilder): EndSessionBuilder {
        return EndSessionBuilder(account)
    }

    fun refreshToken(account: CyberArkAccountBuilder): RefreshTokenBuilder {
        return RefreshTokenBuilder(account)
    }

    fun enroll(account: CyberArkAccountBuilder): EnrollmentBuilder {
        return EnrollmentBuilder(account)
    }

    fun sendFCMToken(account: CyberArkAccountBuilder): SendFCMTokenBuilder {
        return SendFCMTokenBuilder(account)
    }

    fun parseRemoteNotification(remoteMessageData: Map<String, String>): ParseRemoteNotificationBuilder {
        return ParseRemoteNotificationBuilder(remoteMessageData)
    }

    fun otpEnroll(account: CyberArkAccountBuilder): OTPEnrollBuilder {
        return OTPEnrollBuilder(account)
    }

    fun submitOTP(account: CyberArkAccountBuilder): SubmitOTPBuilder {
        return SubmitOTPBuilder(account)
    }

    /**
     * Get authorize token
     *
     * @param intent: Intent object
     * @return Boolean
     */
    @JvmStatic
    fun getAuthorizeToken(intent: Intent?): Boolean {
        if (cyberArkAuthInterface == null) {
            Log.i(TAG, "no previous instance present.")
            return false
        }
        if (cyberArkAuthInterface!!.updateResultForAccessToken(intent)) {
            cleanUp()
        }
        return true
    }

    /**
     * Clean up CyberArk auth instance
     *
     */
    internal fun cleanUp() {
        cyberArkAuthInterface = null
    }

    /**
     * Login builder class
     *
     * @property account: CyberArkAccountBuilder instance
     */
    class LoginBuilder internal constructor(
        private val account: CyberArkAccountBuilder
    ) {
        /**
         * Login user
         *
         * @param context: Activity Context
         * @return LiveData<ResponseHandler<AuthCodeFlowModel>>: LiveData response handler for AuthCodeFlowModel
         */
        fun start(context: Context): LiveData<ResponseHandler<AuthCodeFlowModel>> {
            Log.i(TAG, "Invoke browser login flow")
            cleanUp()
            val cyberarkAuthManager = CyberArkAuthManager(context, account)
            cyberArkAuthInterface = cyberarkAuthManager
            cyberarkAuthManager.startAuthentication()

            return cyberarkAuthManager.getViewModelInstance.getAccessToken()
        }
    }

    /**
     * End session builder class
     *
     * @property account: CyberArkAccountBuilder instance
     */
    class EndSessionBuilder internal constructor(
        private val account: CyberArkAccountBuilder
    ) {
        /**
         * End session
         *
         * @param context: Activity Context
         */
        fun start(context: Context) {
            Log.i(TAG, "Invoke end session flow")
            cleanUp()
            val cyberarkAuthManager = CyberArkAuthManager(context, account)
            cyberArkAuthInterface = cyberarkAuthManager
            cyberarkAuthManager.endSession()
        }
    }

    /**
     * Refresh token builder class
     *
     * @property account: CyberArkAccountBuilder instance
     */
    class RefreshTokenBuilder internal constructor(
        private val account: CyberArkAccountBuilder
    ) {
        /**
         * Get access token using refresh token
         *
         * @param context: Activity Context
         * @param refreshTokenData: refresh token data
         * @return LiveData<ResponseHandler<RefreshTokenModel>>: LiveData response handler for RefreshTokenModel
         */
        fun start(
            context: Context,
            refreshTokenData: String
        ): LiveData<ResponseHandler<RefreshTokenModel>> {
            Log.i(TAG, "Invoke new access token using refresh token")
            val cyberarkAuthManager = CyberArkAuthManager(context, account)
            cyberarkAuthManager.refreshToken(refreshTokenData)

            return cyberarkAuthManager.getViewModelInstance.getRefreshToken()
        }
    }

    /**
     * Enrollment builder class
     *
     */
    class EnrollmentBuilder internal constructor(
        private val account: CyberArkAccountBuilder
    ) {
        /**
         * Enroll device
         *
         * @param context: Activity Context
         * @param accessToken: access token data
         * @return LiveData<ResponseHandler<EnrollmentModel>>: LiveData response handler for EnrollmentModel
         */
        fun start(
            context: Context,
            accessToken: String
        ): LiveData<ResponseHandler<EnrollmentModel>> {
            Log.i(TAG, "Start enroll")
            val cyberarkEnrollmentManager = CyberArkEnrollmentManager(context, accessToken, account)
            cyberarkEnrollmentManager.enroll()

            return cyberarkEnrollmentManager.getViewModelInstance.getEnrolledData()
        }
    }

    /**
     * Send FCM Token builder class
     *
     */
    class SendFCMTokenBuilder internal constructor(
        private val account: CyberArkAccountBuilder
    ) {
        /**
         * Upload FCM token to CyberArk Server
         *
         * @param context: Application / Activity Context
         * @param fcmToken: FCM Token data
         * @param accessToken: access token data
         * @return SendFCMTokenModel
         */
        suspend fun start(
            context: Context,
            fcmToken: String,
            accessToken: String
        ): SendFCMTokenModel? {
            Log.i(TAG, "Upload FCM token")
            val cyberArkFCMTokenManager =
                CyberArkFCMTokenManager(context, fcmToken, accessToken, account)

            return cyberArkFCMTokenManager.uploadFCMToken()
        }
    }

    /**
     * Parse remote notification builder
     *
     * @property remoteMessageData:  Map<String, String>
     */
    class ParseRemoteNotificationBuilder internal constructor(
        private val remoteMessageData: Map<String, String>
    ) {
        /**
         * Decode the notification data
         *
         * @return NotificationDataModel
         */
        fun start(): NotificationDataModel {
            Log.i(TAG, "Parse Remote Notification")
            val cyberArkProcessNotification = CyberArkProcessNotification()

            return cyberArkProcessNotification.parseRemoteNotification(remoteMessageData)
        }
    }

    /**
     * OTP enroll builder
     *
     * @property account: CyberArkAccountBuilder instance
     */
    class OTPEnrollBuilder internal constructor(
        private val account: CyberArkAccountBuilder
    ) {
        /**
         * Call API to get the OTP key and secret that generates OTP code
         *
         * @param context: Application / Activity Context
         * @param accessToken: access token data
         * @return OTPEnrollModel
         */
        suspend fun start(
            context: Context,
            accessToken: String
        ): OTPEnrollModel? {
            Log.i(TAG, "OTP Enroll")
            val cyberArkOTPEnrollManager = CyberArkOTPEnrollManager(context, accessToken, account)

            return cyberArkOTPEnrollManager.otpEnroll()
        }
    }

    /**
     * Submit OTP builder
     *
     * @property account: CyberArkAccountBuilder instance
     */
    class SubmitOTPBuilder internal constructor(
        private val account: CyberArkAccountBuilder
    ) {
        /**
         * Call API to accept /deny the push notification
         *
         * @param context: Application / Activity Context
         * @param accessToken: access token data
         * @param otpEnrollModel: OTPEnrollModel
         * @param notificationPayload: JSONObject
         * @return
         */
        suspend fun start(
            context: Context,
            accessToken: String,
            otpEnrollModel: OTPEnrollModel,
            notificationPayload: JSONObject
        ): SubmitOTPModel? {
            Log.i(TAG, "Submit OTP")
            val cyberArkOTPEnrollManager = CyberArkSubmitOTPManager(
                context,
                accessToken,
                otpEnrollModel,
                notificationPayload,
                account
            )

            return cyberArkOTPEnrollManager.submitOTP()
        }
    }
}