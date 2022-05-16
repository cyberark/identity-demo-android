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

package com.cyberark.identity.provider.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cyberark.identity.activity.CyberArkAuthActivity
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.builder.CyberArkAuthWidgetBuilder
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.data.network.CyberArkAuthService
import com.cyberark.identity.provider.callback.CyberArkAuthWidgetInterface
import com.cyberark.identity.viewmodel.AuthenticationWidgetViewModel
import com.cyberark.identity.viewmodel.base.CyberArkViewModelFactory

/**
 * CyberArk auth widget manager class
 *
 * @property context: Activity context
 * @property account: CyberArkAccountBuilder instance
 */
internal class CyberArkAuthWidgetManager(
    private val context: Context,
    private val account: CyberArkAccountBuilder,
    private val cyberArkAuthWidgetBuilder: CyberArkAuthWidgetBuilder
) : CyberArkAuthWidgetInterface {

    private val tag: String? = CyberArkAuthWidgetManager::class.simpleName
    private val viewModel: AuthenticationWidgetViewModel

    /**
     * Call authorize endpoint using chrome custom tab
     *
     * @param intent: Intent object
     * @return Boolean
     */
    override fun callAuthorizeEndpoint(intent: Intent?): Boolean {
        Log.i(tag, "Received callback for resource url")
        viewModel.handleResourceUrl(intent?.data.toString())
        return true
    }

    /**
     * Start authentication flow in chrome custom tab browser
     *
     */
    internal fun startAuthentication() {
        Log.i(tag, "Inside startAuthentication()")
        CyberArkAuthActivity.authenticateUsingCustomTab(
            context,
            Uri.parse(cyberArkAuthWidgetBuilder.getAuthWidgetBaseURL())
        )
    }

    /**
     * Get authentication view model instance
     */
    internal val getViewModelInstance: AuthenticationWidgetViewModel
        get() = viewModel

    init {
        // Initialize authentication widget view model
        val appContext: AppCompatActivity = context as AppCompatActivity
        val cyberArkAuthService: CyberArkAuthService =
            CyberArkAuthBuilder.getRetrofit(account.getBaseUrl)
                .create(CyberArkAuthService::class.java)
        viewModel = ViewModelProvider(
            appContext,
            CyberArkViewModelFactory(CyberArkAuthHelper(cyberArkAuthService))
        )[AuthenticationWidgetViewModel::class.java]
    }
}