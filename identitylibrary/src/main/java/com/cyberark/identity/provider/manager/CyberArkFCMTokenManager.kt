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
import com.cyberark.identity.data.model.SendFCMTokenModel
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.data.network.CyberArkAuthService
import com.cyberark.identity.util.device.DeviceConstants
import com.cyberark.identity.util.device.DeviceInfoHelper
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.viewmodel.EnrollmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * CyberArk FCM token manager class
 *
 * @property context: Application / Activity context
 * @property fcmToken: FCM Token data
 * @property accessToken: access token data
 * @property account: CyberArkAccountBuilder
 */
internal class CyberArkFCMTokenManager(
    private val context: Context,
    private val fcmToken: String,
    private val accessToken: String,
    private val account: CyberArkAccountBuilder
) {
    private val tag: String? = EnrollmentViewModel::class.simpleName
    private val mediaType: MediaType? = "application/json".toMediaTypeOrNull()

    init {
        Log.i(tag, "initialize CyberArkFCMTokenManager")
    }

    /**
     * Upload FCM token to CyberArk Server
     *
     * @return SendFCMTokenModel
     */
    internal suspend fun uploadFCMToken(): SendFCMTokenModel? {
        var sendFCMTokenData: SendFCMTokenModel? = null
        withContext(Dispatchers.IO) {
            try {
                val cyberArkAuthService: CyberArkAuthService =
                    CyberArkAuthBuilder.getRetrofit(account.getBaseSystemUrl)
                        .create(CyberArkAuthService::class.java)
                val cyberArkAuthHelper = CyberArkAuthHelper(cyberArkAuthService)

                val headerPayload = getHeaderPayload()
                val bodyPayload = getBodyPayload()

                val idapNativeClient: Boolean = headerPayload.getBoolean(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT)
                val bearerToken: String = headerPayload.getString(EndpointUrls.HEADER_AUTHORIZATION)

                sendFCMTokenData = cyberArkAuthHelper.sendFCMToken(idapNativeClient, bearerToken, createJsonBody(bodyPayload.toString()))
            } catch (e: Exception) {
                //TODO.. log added to verify failure case, need to verify and remove later
                Log.i(tag, e.toString())
            }
        }
        return sendFCMTokenData
    }

    /**
     * Get request body payload
     *
     * @return JSONObject
     */
    private fun getBodyPayload(): JSONObject {
        val deviceInfoHelper = DeviceInfoHelper()
        val payload = JSONObject()
        payload.put(DeviceConstants.KEY_DEVICE_ID, deviceInfoHelper.getUDID(context))
        payload.put(DeviceConstants.KEY_FCM_TOKEN, fcmToken)
        Log.i("fcm body payload", payload.toString())
        return payload
    }

    /**
     * Get header payload
     *
     * @return JSONObject
     */
    private fun getHeaderPayload(): JSONObject {
        val payload = JSONObject()
        payload.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT, true)
        payload.put(EndpointUrls.HEADER_AUTHORIZATION, "Bearer $accessToken")
        return payload
    }


    /**
     * Create Json request body
     *
     * @param jsonStr: JSON string
     * @return RequestBody
     */
    private fun createJsonBody(jsonStr: String): RequestBody {
        return jsonStr.toRequestBody(mediaType)
    }
}