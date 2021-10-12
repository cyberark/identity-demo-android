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
     * @param idapNativeClient: IDAP native client
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
     * Enroll device
     *
     * @param centrifyNativeClient: centrify native client
     * @param idapNativeClient: idap native client
     * @param acceptLang: accepted language
     * @param bearerToken: authorization bearer token
     * @param payload: request body
     * @return EnrollmentModel
     */
    @POST(URL_FAST_ENROLL_V3)
    suspend fun fastEnrollV3(
        @Header(EndpointUrls.HEADER_X_CENTRIFY_NATIVE_CLIENT) centrifyNativeClient: Boolean,
        @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
        @Header(EndpointUrls.HEADER_ACCEPT_LANGUAGE) acceptLang: String,
        @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
        @Body payload: RequestBody
    ): EnrollmentModel

    /**
     * Send FCM token
     *
     * @param centrifyNativeClient: centrify native client
     * @param bearerToken: authorization bearer token
     * @param payload: request body
     * @return SendFCMTokenModel
     */
    @POST(URL_UPDATE_DEV_SETTINGS)
    suspend fun sendFCMToken(
        @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) centrifyNativeClient: Boolean,
        @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
        @Body payload: RequestBody
    ): SendFCMTokenModel

}