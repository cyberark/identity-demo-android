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
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.ResponseStatus
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.keystore.KeyStoreManager
import kotlinx.coroutines.launch

/**
 * Authentication view model
 *
 * @property cyberarkAuthHelper
 * @constructor Create empty Authentication view model
 */
internal class AuthenticationViewModel(private val cyberarkAuthHelper: CyberarkAuthHelper) : ViewModel() {

    private val TAG: String? = AuthenticationViewModel::class.simpleName
    private val authResponse = MutableLiveData<ResponseHandler<AuthCodeFlowModel>>()
    private val refreshTokenResponse = MutableLiveData<ResponseHandler<RefreshTokenModel>>()

    init {
        Log.i(TAG, "initialize AuthenticationViewModel")
    }

    /**
     * Handle authorization code
     *
     * @param params
     */
    internal fun handleAuthorizationCode(params: HashMap<String?, String?>) {
        viewModelScope.launch {
            authResponse.postValue(ResponseHandler.loading(null))
            try {
                val accessTokenCreds = cyberarkAuthHelper.getAccessToken(params)
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "accessCredentials :: " + accessTokenCreds.toString())
                //Save Access token we recieved if it is success
//                KeyStoreProvider.get().saveAuthToken(accessTokenCreds.access_token)
//                KeyStoreProvider.get().saveRefreshToken(accessTokenCreds.refresh_token)

                authResponse.postValue(ResponseHandler.success(accessTokenCreds))
            } catch (e: Exception) {
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "Exception :: " + e.toString())
                authResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    /**
     * Handle refresh token
     *
     * @param params
     */
    internal fun handleRefreshToken(params: HashMap<String?, String?>) {
        viewModelScope.launch {
            refreshTokenResponse.postValue(ResponseHandler.loading(null))
            try {
                val refreshTokenCreds = cyberarkAuthHelper.refreshToken(params)
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "refreshTokenCreds :: " + refreshTokenCreds.toString())
                refreshTokenResponse.postValue(ResponseHandler.success(refreshTokenCreds))
            } catch (e: Exception) {
                //TODO.. for testing only added this log and should be removed later
                Log.i(TAG, "Exception :: " + e.toString())
                refreshTokenResponse.postValue(ResponseHandler.error(e.toString(), null))
            }
        }
    }

    /**
     * Get access token
     *
     * @return
     */
    internal fun getAccessToken(): LiveData<ResponseHandler<AuthCodeFlowModel>> {
        return authResponse
    }

    /**
     * Get refresh token
     *
     * @return
     */
    internal fun getRefreshToken(): LiveData<ResponseHandler<RefreshTokenModel>> {
        return refreshTokenResponse
    }
}