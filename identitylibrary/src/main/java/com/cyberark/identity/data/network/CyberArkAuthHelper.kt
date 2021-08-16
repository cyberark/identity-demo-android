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

import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.model.RefreshTokenModel
import okhttp3.RequestBody

/**
 * Cyberark auth helper
 *
 * @property cyberArkAuthService
 * @constructor Create empty Cyberark auth helper
 */
class CyberArkAuthHelper(private val cyberArkAuthService: CyberArkAuthService) {

    /**
     * Qr code login
     *
     * @param dapNativeClient
     * @param bearerToken
     * @param url
     * @return
     */
    suspend fun qrCodeLogin(dapNativeClient: Boolean,
                            bearerToken: String,
                            url: String): QRCodeLoginModel = cyberArkAuthService.qrCodeLogin(dapNativeClient, bearerToken, url)

    /**
     * Get access token
     *
     * @param params
     * @return
     */
    suspend fun getAccessToken(params: HashMap<String?,
            String?>): AuthCodeFlowModel = cyberArkAuthService.getAccessToken(params)

    /**
     * Refresh token
     *
     * @param params
     * @return
     */
    suspend fun refreshToken(params: HashMap<String?,
            String?>): RefreshTokenModel = cyberArkAuthService.refreshToken(params)

    /**
     * Fast enroll v3
     *
     * @param centrifyNativeClient
     * @param idapNativeClient
     * @param acceptLang
     * @param bearerToken
     * @param body
     * @return
     */
    suspend fun fastEnrollV3(centrifyNativeClient: Boolean,
                             idapNativeClient: Boolean,
                             acceptLang: String,
                             bearerToken: String,
                             body: RequestBody): EnrollmentModel = cyberArkAuthService.fastEnrollV3(centrifyNativeClient, idapNativeClient, acceptLang, bearerToken, body)

}