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

package com.cyberark.identity.provider.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.UrlQuerySanitizer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cyberark.identity.activity.CyberArkAuthActivity
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.data.network.CyberArkAuthService
import com.cyberark.identity.provider.CyberArkAuthInterface
import com.cyberark.identity.viewmodel.AuthenticationViewModel
import com.cyberark.identity.viewmodel.base.CyberArkViewModelFactory

/**
 * CyberArk auth manager class
 *
 * @property context: Activity context
 * @property account: CyberArkAccountBuilder instance
 */
internal class CyberArkAuthManager(
    private val context: Context,
    private val account: CyberArkAccountBuilder
) : CyberArkAuthInterface {

    private val tag: String? = CyberArkAuthManager::class.simpleName
    private val viewModel: AuthenticationViewModel

    /**
     * Update result for access token
     *
     * @param intent: Intent object
     * @return Boolean
     */
    override fun updateResultForAccessToken(intent: Intent?): Boolean {
        if(intent?.data != null) {
            val sanitizer = UrlQuerySanitizer()
            sanitizer.allowUnregisteredParamaters = true
            sanitizer.parseUrl(intent.data?.toString())
            val code = sanitizer.getValue(CyberArkAccountBuilder.KEY_CODE)

            if (code != null) {
                val params = HashMap<String?, String?>()
                params[CyberArkAccountBuilder.KEY_GRANT_TYPE] =
                    CyberArkAccountBuilder.AUTHORIZATION_CODE_VALUE
                params[CyberArkAccountBuilder.KEY_CODE] = code.toString()
                params[CyberArkAccountBuilder.KEY_REDIRECT_URI] = account.getRedirectURL
                params[CyberArkAccountBuilder.KEY_CLIENT_ID] = account.getClientId
                params[CyberArkAccountBuilder.KEY_CODE_VERIFIER] = account.getCodeVerifier

                Log.i(tag, "Code exchange for access token")
                viewModel.handleAuthorizationCode(params, account.getOAuthTokenURL)
            } else {
                Log.i(tag, "Unable to fetch code from server to get access token")
                val error = sanitizer.getValue(CyberArkAccountBuilder.KEY_ERROR)
                val decodedError = error?.replace("_", " ").toString()
                val errorDesc = sanitizer.getValue(CyberArkAccountBuilder.KEY_ERROR_DESC)
                val decodedErrorDesc = errorDesc?.replace("_", " ").toString()
                viewModel.handleLoginError(decodedError, decodedErrorDesc)
            }
        } else {
            Log.i(tag, "Intent data is null")
        }
        return true
    }

    /**
     * Refresh token API call to get new access token
     *
     * @param refreshTokenData: refresh token string
     */
    internal fun refreshToken(refreshTokenData: String?) {
        val params = HashMap<String?, String?>()
        params[CyberArkAccountBuilder.KEY_CLIENT_ID] = account.getClientId
        params[CyberArkAccountBuilder.KEY_GRANT_TYPE] = CyberArkAccountBuilder.KEY_REFRESH_TOKEN
        params[CyberArkAccountBuilder.KEY_REFRESH_TOKEN] = refreshTokenData

        if (refreshTokenData != null) {
            Log.i(tag, "Get new access token using refresh token")
            viewModel.handleRefreshToken(params, account.getOAuthTokenURL)
        } else {
            Log.i(tag, "Unable to fetch access token using refresh token")
            // TODO.. handle error, throw exception
        }
    }

    /**
     * Start authentication flow in chrome custom tab browser
     *
     */
    internal fun startAuthentication() {
        CyberArkAuthActivity.authenticateUsingCustomTab(
            context,
            Uri.parse(account.getOAuthBaseURL)
        )
    }

    /**
     * End session from the chrome custom tab browser
     *
     */
    internal fun endSession() {
        CyberArkAuthActivity.authenticateUsingCustomTab(
            context,
            Uri.parse(account.getOAuthEndSessionURL)
        )
    }

    /**
     * Get authentication view model instance
     */
    internal val getViewModelInstance: AuthenticationViewModel
        get() = viewModel

    init {
        // Initialize authentication view model
        val appContext: AppCompatActivity = context as AppCompatActivity
        val cyberArkAuthService: CyberArkAuthService =
            CyberArkAuthBuilder.getRetrofit(account.getBaseUrl)
                .create(CyberArkAuthService::class.java)
        viewModel = ViewModelProvider(
            appContext,
            CyberArkViewModelFactory(CyberArkAuthHelper(cyberArkAuthService))
        ).get(AuthenticationViewModel::class.java)
    }

}