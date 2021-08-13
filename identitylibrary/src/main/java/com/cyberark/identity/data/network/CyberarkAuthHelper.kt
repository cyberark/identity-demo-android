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

class CyberarkAuthHelper(private val cyberarkAuthService: CyberarkAuthService) {

    suspend fun qrCodeLogin(dapNativeClient: Boolean,
                            bearerToken: String,
                            url: String): QRCodeLoginModel = cyberarkAuthService.qrCodeLogin(dapNativeClient, bearerToken, url)

    suspend fun getAccessToken(params: HashMap<String?,
            String?>): AuthCodeFlowModel = cyberarkAuthService.getAccessToken(params)

    suspend fun refreshToken(params: HashMap<String?,
            String?>): RefreshTokenModel = cyberarkAuthService.refreshToken(params)

    suspend fun fastEnrollV3(centrifyNativeClient: Boolean,
                             idapNativeClient: Boolean,
                             acceptLang: String,
                             bearerToken: String,
                             body: RequestBody): EnrollmentModel = cyberarkAuthService.fastEnrollV3(centrifyNativeClient, idapNativeClient, acceptLang, bearerToken, body)

}