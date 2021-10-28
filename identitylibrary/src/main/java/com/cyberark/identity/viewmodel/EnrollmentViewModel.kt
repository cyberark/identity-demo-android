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
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.endpoint.EndpointUrls
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Enrollment view model
 * 1. Handle device enrollment
 *
 * @property cyberArkAuthHelper: CyberArkAuthHelper instance
 */
internal class EnrollmentViewModel(private val cyberArkAuthHelper: CyberArkAuthHelper) :
    ViewModel() {

    private val tag: String? = EnrollmentViewModel::class.simpleName
    private val enrolledResponse = MutableLiveData<ResponseHandler<EnrollmentModel>>()
    private val mediaType: MediaType? = "application/json".toMediaTypeOrNull()

    init {
        Log.i(tag, "initialize EnrollViewModel")
    }

    /**
     * Handle enrollment API call
     *
     * @param headerPayload: header payload
     * @param bodyPayload: body payload
     */
    internal fun handleEnrollment(headerPayload: JSONObject, bodyPayload: JSONObject) {
        viewModelScope.launch {
            enrolledResponse.postValue(ResponseHandler.loading(null))
            try {
                val idapNativeClient: Boolean =
                    headerPayload.getBoolean(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT)
                val acceptLang: String =
                    headerPayload.getString(EndpointUrls.HEADER_ACCEPT_LANGUAGE)
                val bearerToken: String = headerPayload.getString(EndpointUrls.HEADER_AUTHORIZATION)

                val enrollmentData = cyberArkAuthHelper.fastEnrollV3(
                    idapNativeClient,
                    acceptLang,
                    bearerToken,
                    createJsonBody(bodyPayload.toString())
                )
                enrolledResponse.postValue(ResponseHandler.success(enrollmentData))
            } catch (e: Exception) {
                enrolledResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
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

    /**
     * Get enrolled data
     *
     * @return: LiveData<ResponseHandler<EnrollmentModel>>, LiveData ResponseHandler for EnrollmentModel
     */
    internal fun getEnrolledData(): LiveData<ResponseHandler<EnrollmentModel>> {
        return enrolledResponse
    }
}