/*
 * Copyright (c) 2022 CyberArk Software Ltd. All rights reserved.
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

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberark.identity.data.model.SignupCaptchaModel
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Signup with captcha view model
 *
 * @property cyberArkAuthHelper: CyberArkAuthHelper instance
 */
internal class SignupWithCaptchaViewModel(private val cyberArkAuthHelper: CyberArkAuthHelper) :
    ViewModel() {

    private val tag: String? = SignupWithCaptchaViewModel::class.simpleName
    private val signupResponse = MutableLiveData<ResponseHandler<SignupCaptchaModel>>()
    private val mediaType: MediaType? = "application/json".toMediaTypeOrNull()

    init {
        Log.i(tag, "initialize SignupWithCaptchaViewModel")
    }

    /**
     * Handle Signup with captcha
     *
     * @param context: Application / Activity context
     * @param headerPayload: header payload
     * @param bodyPayload: body payload
     * @param siteKey: siteKey for captcha validation
     */
    internal fun handleSignupWithCaptcha(
        context: Context,
        headerPayload: JSONObject,
        bodyPayload: JSONObject,
        siteKey: String
    ) {
        viewModelScope.launch {
            verifyRecaptcha(context, headerPayload, bodyPayload, siteKey)
        }
    }

    /**
     * Verify recaptcha challenge
     *
     * @param context: Application / Activity context
     * @param headerPayload: header payload
     * @param bodyPayload: body payload
     * @param siteKey: recaptcha site key
     */
    private fun verifyRecaptcha(
        context: Context,
        headerPayload: JSONObject,
        bodyPayload: JSONObject,
        siteKey: String
    ) {
        SafetyNet.getClient(context).verifyWithRecaptcha(siteKey)
            .addOnSuccessListener { response ->
                // Indicates communication with reCAPTCHA service was successful.
                val userResponseToken = response.tokenResult
                if (userResponseToken!!.isNotEmpty()) {
                    // Validate the user response token using the reCAPTCHA site verify API.
                    viewModelScope.launch {
                        bodyPayload.put("ReCaptchaToken", userResponseToken)
                        submitSignupData(headerPayload, bodyPayload)
                    }
                }
            }
            .addOnFailureListener { error ->
                if (error is ApiException) {
                    // An error occurred when communicating with the reCAPTCHA service.
                    Log.d(
                        tag,
                        ("Error message: " + CommonStatusCodes.getStatusCodeString(error.statusCode))
                    )
                    signupResponse.postValue(
                        ResponseHandler.error(
                            error.statusCode.toString(),
                            null
                        )
                    )
                } else {
                    // A different, unknown type of error occurred.
                    Log.d(tag, "Unknown type of error: " + error.message)
                    signupResponse.postValue(ResponseHandler.error(error.message.toString(), null))
                }
            }
    }

    /**
     * Submit signup data
     *
     * @param headerPayload: header payload
     * @param bodyPayload: body payload
     */
    private suspend fun submitSignupData(headerPayload: JSONObject, bodyPayload: JSONObject) {
        signupResponse.postValue(ResponseHandler.loading(null))
        try {
            val idapNativeClient: Boolean =
                headerPayload.getBoolean(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT)

            val enrollmentData = cyberArkAuthHelper.signupWithCaptcha(
                idapNativeClient,
                createJsonBody(bodyPayload.toString())
            )
            signupResponse.postValue(ResponseHandler.success(enrollmentData))
        } catch (e: Exception) {
            signupResponse.postValue(ResponseHandler.error(e.toString(), null))
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
     * Get signup with captcha data
     *
     * @return: LiveData<ResponseHandler<SignupCaptchaModel>>, LiveData ResponseHandler for SignupCaptchaModel
     */
    internal fun getSignupWithCaptchaData(): LiveData<ResponseHandler<SignupCaptchaModel>> {
        return signupResponse
    }
}