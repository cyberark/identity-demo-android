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
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.data.network.CyberArkAuthService
import com.cyberark.identity.util.device.DeviceInfoHelper
import com.cyberark.identity.viewmodel.EnrollmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * CyberArk OTP enroll manager class
 *
 * @property context: Application / Activity context
 * @property accessToken: access token data
 * @property account: CyberArkAccountBuilder
 */
internal class CyberArkOTPEnrollManager(
    private val context: Context,
    private val accessToken: String,
    private val account: CyberArkAccountBuilder
) {
    private val tag: String? = EnrollmentViewModel::class.simpleName

    companion object {
        private const val KEY_UDID = "udid"
    }

    init {
        Log.i(tag, "initialize CyberArkOTPEnrollManager")
    }

    /**
     * Upload FCM token to CyberArk Server
     *
     * @return OTPEnrollModel
     */
    internal suspend fun otpEnroll(): OTPEnrollModel? {
        var otpEnrollModel: OTPEnrollModel? = null
        withContext(Dispatchers.IO) {
            try {
                val cyberArkAuthService: CyberArkAuthService =
                    CyberArkAuthBuilder.getRetrofit(account.getBaseSystemUrl)
                        .create(CyberArkAuthService::class.java)
                val cyberArkAuthHelper = CyberArkAuthHelper(cyberArkAuthService)

                otpEnrollModel = cyberArkAuthHelper.otpEnroll("Bearer $accessToken", getOTPEnrollURL)
            } catch (e: Exception) {
                //TODO.. log added to verify failure case, need to verify and remove later
                Log.i(tag, e.toString())
            }
        }
        return otpEnrollModel
    }

    /**
     * Get OTP enroll URL
     */
    private val getOTPEnrollURL: String
        get() = account.getBaseSystemUrl.toHttpUrlOrNull()!!.newBuilder()
            .addPathSegment("IosAppRest")
            .addPathSegment("OtpEnroll")
            .addQueryParameter(KEY_UDID, getDeviceUDID())
            .build()
            .toString()

    /**
     * Get device UDID
     *
     * @return String: device UDID
     */
    private fun getDeviceUDID(): String {
        val deviceInfoHelper = DeviceInfoHelper()
        return deviceInfoHelper.getUDID(context)
    }
}