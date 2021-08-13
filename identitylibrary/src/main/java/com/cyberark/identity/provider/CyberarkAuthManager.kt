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
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.data.network.CyberarkAuthBuilder
import com.cyberark.identity.data.network.CyberarkAuthHelper
import com.cyberark.identity.viewmodel.AuthenticationViewModel
import com.cyberark.identity.viewmodel.base.CyberarkViewModelFactory

internal class CyberarkAuthManager(
        private val context: Context,
        private val account: CyberarkAccountBuilder
) : CyberarkAuthInterface {

    private val TAG: String? = CyberarkAuthManager::class.simpleName
    private val viewModel: AuthenticationViewModel

    override fun updateResult(intent: Intent?): Boolean {
        val code = intent?.data?.getQueryParameter(CyberarkAccountBuilder.KEY_CODE)

        val params = HashMap<String?, String?>()
        params[CyberarkAccountBuilder.KEY_GRANT_TYPE] = CyberarkAccountBuilder.AUTHORIZATION_CODE_VALUE
        params[CyberarkAccountBuilder.KEY_CODE] = code.toString()
        params[CyberarkAccountBuilder.KEY_REDIRECT_URI] = account.getRedirectURL
        params[CyberarkAccountBuilder.KEY_CLIENT_ID] = account.getClientId
        params[CyberarkAccountBuilder.KEY_CODE_VERIFIER] = account.getCodeVerifier

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

    internal fun refreshToken(refreshTokenData: String) {
        val params = HashMap<String?, String?>()
        params[CyberarkAccountBuilder.KEY_CLIENT_ID] = account.getClientId
        params[CyberarkAccountBuilder.KEY_GRANT_TYPE] = CyberarkAccountBuilder.KEY_REFRESH_TOKEN
        params[CyberarkAccountBuilder.KEY_REFRESH_TOKEN] = refreshTokenData

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

    internal fun startAuthentication() {
        CyberarkAuthActivity.authenticateUsingCustomTab(
                context,
                Uri.parse(account.OAuthBaseURL)
        )
    }

    internal fun endSession() {
        CyberarkAuthActivity.authenticateUsingCustomTab(
                context,
                Uri.parse(account.OAuthEndSessionURL)
        )
    }

    internal val getViewModelInstance: AuthenticationViewModel
        get() = viewModel

    init {
        val appContext: AppCompatActivity = context as AppCompatActivity
        viewModel = ViewModelProviders.of(
                appContext,
                CyberarkViewModelFactory(CyberarkAuthHelper(CyberarkAuthBuilder.cyberarkAuthService))
        ).get(AuthenticationViewModel::class.java)
    }

}