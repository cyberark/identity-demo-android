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

package com.cyberark.identity.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberark.identity.data.model.QRCodeLoginModel
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.endpoint.EndpointUrls
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Scan QR code view model
 * 1. Handle QR Code authenticator result
 *
 * @property cyberArkAuthHelper: CyberArkAuthHelper instance
 */
internal class ScanQRCodeViewModel(private val cyberArkAuthHelper: CyberArkAuthHelper) : ViewModel() {

    private val tag: String? = ScanQRCodeViewModel::class.simpleName
    private val qrCodeResponse = MutableLiveData<ResponseHandler<QRCodeLoginModel>>()

    init {
        Log.i(tag, "initialize ScanQRCodeViewModel")
    }

    /**
     * Handle QR code authenticator result
     *
     * @param headerPayload: header payload
     * @param qrCodeUrl: QR Code URL
     */
    internal fun handleQRCodeResult(headerPayload: JSONObject, qrCodeUrl: String) {
        viewModelScope.launch {
            qrCodeResponse.postValue(ResponseHandler.loading(null))
            try {

                val idapNativeClient: Boolean = headerPayload.getBoolean(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT)
                val bearerToken: String = headerPayload.getString(EndpointUrls.HEADER_AUTHORIZATION)

                val usersFromApi = cyberArkAuthHelper.qrCodeLogin(idapNativeClient, bearerToken, qrCodeUrl)
                qrCodeResponse.postValue(ResponseHandler.success(usersFromApi))
            } catch (e: Exception) {
                qrCodeResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    /**
     * Get QR code login response
     *
     * @return LiveData<ResponseHandler<QRCodeLoginModel>>, LiveData ResponseHandler for QRCodeLoginModel
     */
    internal fun qrCodeLogin(): LiveData<ResponseHandler<QRCodeLoginModel>> {
        return qrCodeResponse
    }
}