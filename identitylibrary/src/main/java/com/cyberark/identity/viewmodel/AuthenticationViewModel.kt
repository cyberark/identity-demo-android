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
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import kotlinx.coroutines.launch

/**
 * Authentication view model
 * 1. handle authorization code exchange for access token
 * 2. Get new access token using refresh token
 *
 * @property cyberArkAuthHelper: CyberArkAuthHelper instance
 */
internal class AuthenticationViewModel(private val cyberArkAuthHelper: CyberArkAuthHelper) : ViewModel() {

    private val tag: String? = AuthenticationViewModel::class.simpleName
    private val authResponse = MutableLiveData<ResponseHandler<AuthCodeFlowModel>>()
    private val refreshTokenResponse = MutableLiveData<ResponseHandler<RefreshTokenModel>>()

    init {
        Log.i(tag, "initialize AuthenticationViewModel")
    }

    /**
     * Handle authorization code exchange for access token
     *
     * @param params: HashMap<String?, String?>, request body
     */
    internal fun handleAuthorizationCode(params: HashMap<String?, String?>) {
        viewModelScope.launch {
            authResponse.postValue(ResponseHandler.loading(null))
            try {
                val accessTokenCreds = cyberArkAuthHelper.getAccessToken(params)
                authResponse.postValue(ResponseHandler.success(accessTokenCreds))
            } catch (e: Exception) {
                authResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    /**
     * Handle refresh token API call to get new access token
     *
     * @param params: HashMap<String?, String?>, request body
     */
    internal fun handleRefreshToken(params: HashMap<String?, String?>) {
        viewModelScope.launch {
            refreshTokenResponse.postValue(ResponseHandler.loading(null))
            try {
                val refreshTokenCreds = cyberArkAuthHelper.refreshToken(params)
                refreshTokenResponse.postValue(ResponseHandler.success(refreshTokenCreds))
            } catch (e: Exception) {
                refreshTokenResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    /**
     * Get access token
     *
     * @return LiveData<ResponseHandler<AuthCodeFlowModel>>: LiveData ResponseHandler for AuthCodeFlowModel
     */
    internal fun getAccessToken(): LiveData<ResponseHandler<AuthCodeFlowModel>> {
        return authResponse
    }

    /**
     * Get refresh token
     *
     * @return LiveData<ResponseHandler<RefreshTokenModel>>: LiveData ResponseHandler for RefreshTokenModel
     */
    internal fun getRefreshToken(): LiveData<ResponseHandler<RefreshTokenModel>> {
        return refreshTokenResponse
    }
}