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

package com.cyberark.identity.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.cyberark.identity.CyberarkAuthActivity
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.viewmodel.AuthenticationViewModel
import com.cyberark.identity.viewmodel.base.CyberarkViewModelFactory

/**
 * Cyberark auth manager
 *
 * @property context
 * @property account
 * @constructor Create empty Cyberark auth manager
 */
internal class CyberArkAuthManager(
        private val context: Context,
        private val account: CyberArkAccountBuilder
) : CyberArkAuthInterface {

    private val TAG: String? = CyberArkAuthManager::class.simpleName
    private val viewModel: AuthenticationViewModel

    /**
     * Update result
     *
     * @param intent
     * @return
     */
    override fun updateResult(intent: Intent?): Boolean {
        val code = intent?.data?.getQueryParameter(CyberArkAccountBuilder.KEY_CODE)

        val params = HashMap<String?, String?>()
        params[CyberArkAccountBuilder.KEY_GRANT_TYPE] = CyberArkAccountBuilder.AUTHORIZATION_CODE_VALUE
        params[CyberArkAccountBuilder.KEY_CODE] = code.toString()
        params[CyberArkAccountBuilder.KEY_REDIRECT_URI] = account.getRedirectURL
        params[CyberArkAccountBuilder.KEY_CLIENT_ID] = account.getClientId
        params[CyberArkAccountBuilder.KEY_CODE_VERIFIER] = account.getCodeVerifier

        //TODO.. for testing only added this log and should be removed later
        Log.i(TAG, "params" + params.toString())

        if (code != null) {
            Log.i(TAG, "Code exchange for access token")
            viewModel.handleAuthorizationCode(params)
        } else {
            Log.i(TAG, "Unable to fetch code from server to get access token")
            // TODO.. handle error
        }
        return true
    }

    /**
     * Refresh token
     *
     * @param refreshTokenData
     */
    internal fun refreshToken(refreshTokenData: String) {
        val params = HashMap<String?, String?>()
        params[CyberArkAccountBuilder.KEY_CLIENT_ID] = account.getClientId
        params[CyberArkAccountBuilder.KEY_GRANT_TYPE] = CyberArkAccountBuilder.KEY_REFRESH_TOKEN
        params[CyberArkAccountBuilder.KEY_REFRESH_TOKEN] = refreshTokenData

        //TODO.. for testing only added this log and should be removed later
        Log.i(TAG, "params" + params.toString())

        if (refreshTokenData != null) {
            Log.i(TAG, "Get new access token using refresh token")
            viewModel.handleRefreshToken(params)
        } else {
            Log.i(TAG, "Unable to fetch access token using refresh token")
            // TODO.. handle error
        }
    }

    /**
     * Start authentication
     *
     */
    internal fun startAuthentication() {
        CyberarkAuthActivity.authenticateUsingCustomTab(
                context,
                Uri.parse(account.OAuthBaseURL)
        )
    }

    /**
     * End session
     *
     */
    internal fun endSession() {
        CyberarkAuthActivity.authenticateUsingCustomTab(
                context,
                Uri.parse(account.OAuthEndSessionURL)
        )
    }

    /**
     * Get view model instance
     */
    internal val getViewModelInstance: AuthenticationViewModel
        get() = viewModel

    init {
        val appContext: AppCompatActivity = context as AppCompatActivity
        viewModel = ViewModelProviders.of(
                appContext,
                CyberarkViewModelFactory(CyberArkAuthHelper(CyberArkAuthBuilder.CYBER_ARK_AUTH_SERVICE))
        ).get(AuthenticationViewModel::class.java)
    }

}