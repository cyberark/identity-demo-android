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
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_AUTH_CODE_FLOW
import com.cyberark.identity.util.endpoint.EndpointUrls.URL_FAST_ENROLL_V3
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Cyberark auth service
 *
 * @constructor Create empty Cyberark auth service
 */
interface CyberArkAuthService {

    /**
     * Qr code login
     *
     * @param idapNativeClient
     * @param bearerToken
     * @param url
     * @return
     */
    @POST
    suspend fun qrCodeLogin(@Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
                            @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
                            @Url url: String): QRCodeLoginModel

    /**
     * Get access token
     *
     * @param params
     * @return
     */
    @FormUrlEncoded
    @POST(URL_AUTH_CODE_FLOW)
    suspend fun getAccessToken(@FieldMap params: HashMap<String?, String?>): AuthCodeFlowModel

    /**
     * Refresh token
     *
     * @param params
     * @return
     */
    @FormUrlEncoded
    @POST(URL_AUTH_CODE_FLOW)
    suspend fun refreshToken(@FieldMap params: HashMap<String?, String?>): RefreshTokenModel

    /**
     * Fast enroll v3
     *
     * @param centrifyNativeClient
     * @param idapNativeClient
     * @param acceptLang
     * @param bearerToken
     * @param payload
     * @return
     */
    @POST(URL_FAST_ENROLL_V3)
    suspend fun fastEnrollV3(@Header(EndpointUrls.HEADER_X_CENTRIFY_NATIVE_CLIENT) centrifyNativeClient: Boolean,
                             @Header(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT) idapNativeClient: Boolean,
                             @Header(EndpointUrls.HEADER_ACCEPT_LANGUAGE) acceptLang: String,
                             @Header(EndpointUrls.HEADER_AUTHORIZATION) bearerToken: String,
                             @Body payload: RequestBody): EnrollmentModel

}