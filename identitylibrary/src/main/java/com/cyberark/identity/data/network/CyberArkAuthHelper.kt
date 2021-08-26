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
    ): QRCodeLoginModel = cyberArkAuthService.qrCodeLogin(idapNativeClient, bearerToken, url)

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
    ): AuthCodeFlowModel = cyberArkAuthService.getAccessToken(params, url)

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
     *
     * @param centrifyNativeClient: centrify native client
     * @param idapNativeClient: idap native client
     * @param acceptLang: accepted language
     * @param bearerToken: authorization bearer token
     * @param body: request body
     * @return EnrollmentModel
     */
    suspend fun fastEnrollV3(
        centrifyNativeClient: Boolean,
        idapNativeClient: Boolean,
        acceptLang: String,
        bearerToken: String,
        body: RequestBody
    ): EnrollmentModel = cyberArkAuthService.fastEnrollV3(
        centrifyNativeClient,
        idapNativeClient,
        acceptLang,
        bearerToken,
        body
    )

}