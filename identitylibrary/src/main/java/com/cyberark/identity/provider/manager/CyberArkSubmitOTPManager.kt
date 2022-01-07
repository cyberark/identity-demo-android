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

package com.cyberark.identity.provider.manager

import android.content.Context
import android.util.Log
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.OTPEnrollModel
import com.cyberark.identity.data.model.SubmitOTPModel
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.data.network.CyberArkAuthService
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.util.notification.TOTPManager
import com.cyberark.identity.viewmodel.EnrollmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * CyberArk submit OTP manager class
 *
 * @property context: Application / Activity context
 * @property accessToken: access token data
 * @property otpEnrollModel: OTPEnrollModel
 * @property notificationPayload: JSONObject
 * @property account: CyberArkAccountBuilder
 */
internal class CyberArkSubmitOTPManager(
    private val context: Context,
    private val accessToken: String,
    private val otpEnrollModel: OTPEnrollModel,
    private val notificationPayload: JSONObject,
    private val account: CyberArkAccountBuilder
) {
    private val tag: String? = EnrollmentViewModel::class.simpleName

    init {
        Log.i(tag, "initialize CyberArkOTPEnrollManager")
    }

    /**
     * Send OTP code and notification accepted status to CyberArk Server
     *
     * @return SubmitOTPModel
     */
    internal suspend fun submitOTP(): SubmitOTPModel? {
        var submitOTPModel: SubmitOTPModel? = null
        withContext(Dispatchers.IO) {
            try {
                val cyberArkAuthService: CyberArkAuthService =
                    CyberArkAuthBuilder.getRetrofit(account.getBaseSystemUrl)
                        .create(CyberArkAuthService::class.java)
                val cyberArkAuthHelper = CyberArkAuthHelper(cyberArkAuthService)

                val challengeAnswer: String =
                    notificationPayload.getString(EndpointUrls.QUERY_OTP_CHALLENGE_ANSWER)
                val userAccepted: Boolean =
                    notificationPayload.getBoolean(EndpointUrls.QUERY_USER_ACCEPTED)

                submitOTPModel = cyberArkAuthHelper.submitOTPCode(
                    "Bearer $accessToken",
                    getOTPCode(),
                    otpEnrollModel.Result.OTPKeyVersion,
                    getOTPTimestamp(),
                    userAccepted,
                    otpEnrollModel.Result.OTPCodeExpiryInterval,
                    challengeAnswer,
                    otpEnrollModel.Result.OathProfileUuid
                )
            } catch (e: Exception) {
                //TODO.. log added to verify failure case, need to verify and remove later
                Log.i(tag, e.toString())
            }
        }
        return submitOTPModel
    }

    /**
     * Get OTP code from TOTPGenerator class
     *
     * @return String
     */
    private fun getOTPCode(): String {
        return TOTPManager.generateTOTP(otpEnrollModel)
    }

    /**
     * Get OTP code generated timestamp
     *
     * @return Long
     */
    private fun getOTPTimestamp(): Long {
        return System.currentTimeMillis()
    }
}