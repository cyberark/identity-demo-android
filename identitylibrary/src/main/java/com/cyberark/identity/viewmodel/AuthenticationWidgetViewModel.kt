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

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import kotlinx.coroutines.launch

/**
 * Authentication widget view model
 * handle resource url callback
 *
 * @property cyberArkAuthHelper: CyberArkAuthHelper instance
 */
internal class AuthenticationWidgetViewModel(private val cyberArkAuthHelper: CyberArkAuthHelper) :
    ViewModel() {

    private val tag: String? = AuthenticationWidgetViewModel::class.simpleName
    private val resourceUrlResponse = MutableLiveData<ResponseHandler<String>>()

    init {
        Log.i(tag, "initialize AuthenticationWidgetViewModel")
    }

    /**
     * Handle resource url
     *
     * @param resourceUrl: resource URL
     */
    internal fun handleResourceUrl(resourceUrl: String) {
        viewModelScope.launch {
            resourceUrlResponse.postValue(ResponseHandler.loading(null))
            if(resourceUrl.contains("/resourceURLCallback")) {
                resourceUrlResponse.postValue(ResponseHandler.success(resourceUrl))
            } else {
                resourceUrlResponse.postValue(ResponseHandler.error("Invalid callback URL $resourceUrl", null))
            }
        }
    }

    /**
     * Get resource url
     *
     * @return LiveData<ResponseHandler<String>>: LiveData ResponseHandler for resource url string
     */
    internal fun getResourceUrl(): LiveData<ResponseHandler<String>> {
        return resourceUrlResponse
    }
}