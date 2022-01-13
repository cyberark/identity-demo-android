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
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cyberark.identity.builder.CyberArkWidgetBuilder
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.data.network.CyberArkAuthService
import com.cyberark.identity.util.device.DeviceConstants
import com.cyberark.identity.viewmodel.BasicLoginViewModel
import com.cyberark.identity.viewmodel.base.CyberArkViewModelFactory
import org.json.JSONObject

/**
 * CyberArk basic login manager class
 *
 * @property context: Activity Context
 * @property username: login user name
 * @property password: login password
 */
internal class CyberArkBasicLoginManager(
    private val context: Context,
    private val username: String,
    private val password: String,
    widgetBuilder: CyberArkWidgetBuilder
) {
    private val viewModel: BasicLoginViewModel

    /**
     * Handle basic login
     */
    internal fun basicLogin() {
        viewModel.handleBasicLogin(getBodyPayload())
    }

    /**
     * Get view model instance
     */
    internal val getViewModelInstance: BasicLoginViewModel
        get() = viewModel

    init {
        // Initialize BasicLoginViewModel
        Log.i("widgetBuilder.getNativeLoginURL :: ", widgetBuilder.getNativeLoginURL)
        val appContext: AppCompatActivity = context as AppCompatActivity
        val cyberArkAuthService: CyberArkAuthService =
            CyberArkAuthBuilder.getRetrofit(widgetBuilder.getNativeLoginURL)
                .create(CyberArkAuthService::class.java)
        viewModel = ViewModelProvider(
            appContext,
            CyberArkViewModelFactory(CyberArkAuthHelper(cyberArkAuthService))
        )[BasicLoginViewModel::class.java]
    }

    /**
     * Get request body payload
     *
     * @return JSONObject
     */
    private fun getBodyPayload(): JSONObject {
        val payload = JSONObject()
        payload.put(DeviceConstants.KEY_BASIC_LOGIN_USERNAME, username)
        payload.put(DeviceConstants.KEY_BASIC_LOGIN_PASSWORD, password)
        return payload
    }
}