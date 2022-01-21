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
import com.cyberark.identity.data.model.BasicLoginModel
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException

/**
 * Basic login view model
 *
 * @property cyberArkAuthHelper: CyberArkAuthHelper instance
 */
internal class BasicLoginViewModel(private val cyberArkAuthHelper: CyberArkAuthHelper) :
    ViewModel() {

    private val tag: String? = BasicLoginViewModel::class.simpleName
    private val basicLoginResponse = MutableLiveData<ResponseHandler<BasicLoginModel>>()
    private val mediaType: MediaType? = "application/json".toMediaTypeOrNull()

    init {
        Log.i(tag, "initialize BasicLoginViewModel")
    }

    /**
     * Handle basic login API call
     *
     * @param bodyPayload: body payload
     */
    internal fun handleBasicLogin(bodyPayload: JSONObject) {
        viewModelScope.launch {
            basicLoginResponse.postValue(ResponseHandler.loading(null))
            try {
                val basicLoginData = cyberArkAuthHelper.basicLogin(
                    createJsonBody(bodyPayload.toString())
                )
                basicLoginResponse.postValue(ResponseHandler.success(basicLoginData))
            } catch (e: HttpException) {
                basicLoginResponse.postValue(ResponseHandler.error(e.toString(), null))
            } catch (e: Exception) {
                basicLoginResponse.postValue(ResponseHandler.error(e.toString(), null))
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
     * Get basic login data
     *
     * @return: LiveData<ResponseHandler<BasicLoginModel>>, LiveData ResponseHandler for BasicLoginModel
     */
    internal fun getBasicLoginData(): LiveData<ResponseHandler<BasicLoginModel>> {
        return basicLoginResponse
    }
}