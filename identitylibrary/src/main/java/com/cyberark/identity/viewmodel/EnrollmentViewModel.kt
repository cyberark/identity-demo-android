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
 *
 * @property cyberArkAuthHelper
 * @constructor Create empty Enrollment view model
 */
internal class EnrollmentViewModel(private val cyberArkAuthHelper: CyberArkAuthHelper) :
    ViewModel() {

    private val TAG: String? = EnrollmentViewModel::class.simpleName
    private val enrolledResponse = MutableLiveData<ResponseHandler<EnrollmentModel>>()

    private val mediaType: MediaType? = "application/json".toMediaTypeOrNull()

    init {
        Log.i(TAG, "initialize EnrollViewModel")
    }

    /**
     * Handle enrollment
     *
     * @param headerPayload
     * @param bodyPayload
     */
    internal fun handleEnrollment(headerPayload: JSONObject, bodyPayload: JSONObject) {
        viewModelScope.launch {
            enrolledResponse.postValue(ResponseHandler.loading(null))
            try {
                //TODO.. for testing only added logs, should be removed
                Log.i(TAG, "headerPayload :: " + headerPayload.toString())
                Log.i(TAG, "bodyPayload :: " + createJsonBody(bodyPayload.toString()))

                val centrifyNativeClient: Boolean =
                    headerPayload.getBoolean(EndpointUrls.HEADER_X_CENTRIFY_NATIVE_CLIENT)
                val idapNativeClient: Boolean =
                    headerPayload.getBoolean(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT)
                val acceptLang: String =
                    headerPayload.getString(EndpointUrls.HEADER_ACCEPT_LANGUAGE)
                val bearerToken: String = headerPayload.getString(EndpointUrls.HEADER_AUTHORIZATION)

                //TODO.. for testing only added logs, should be removed
                Log.i(TAG, "headerPayload 1:: " + centrifyNativeClient)
                Log.i(TAG, "headerPayload 2:: " + idapNativeClient)
                Log.i(TAG, "headerPayload 3:: " + acceptLang)
                Log.i(TAG, "headerPayload 4:: " + bearerToken)

                val enrollmentData = cyberArkAuthHelper.fastEnrollV3(
                    centrifyNativeClient,
                    idapNativeClient,
                    acceptLang,
                    bearerToken,
                    createJsonBody(bodyPayload.toString())
                )

                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "enrollmentResponse :: " + enrollmentData.toString())
                Log.i(TAG, "enrollmentResponse 11:: " + enrollmentData.success)
//                Log.i(TAG, "enrollmentResponse 11 :: " + enrollmentResponse.execute().body().toString())

                enrolledResponse.postValue(ResponseHandler.success(enrollmentData))
            } catch (e: Exception) {
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "Exception :: " + e.toString())
                enrolledResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    /**
     * Create json body
     *
     * @param jsonStr
     * @return
     */
    private fun createJsonBody(jsonStr: String): RequestBody {
        //TODO.. verify deprecation warning and refactor code as needed
        return jsonStr.toRequestBody(mediaType)
    }

    /**
     * Get enrolled data
     *
     * @return
     */
    internal fun getEnrolledData(): LiveData<ResponseHandler<EnrollmentModel>> {
        return enrolledResponse
    }
}