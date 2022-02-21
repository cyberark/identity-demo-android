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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.network.CyberArkAuthBuilder
import com.cyberark.identity.data.network.CyberArkAuthHelper
import com.cyberark.identity.data.network.CyberArkAuthService
import com.cyberark.identity.util.endpoint.EndpointUrls
import com.cyberark.identity.viewmodel.SignupWithCaptchaViewModel
import com.cyberark.identity.viewmodel.base.CyberArkViewModelFactory
import org.json.JSONObject

/**
 * CyberArk signup manager class
 *
 * @property context: Application / Activity context
 * @property signupData: signup form data
 * @property siteKey: siteKey for captcha validation
 * @property account: CyberArkAccountBuilder
 *
 */
internal class CyberArkSignupManager(
    private val context: Context,
    private val signupData: JSONObject,
    private val siteKey: String,
    private val account: CyberArkAccountBuilder
) {
    private val viewModel: SignupWithCaptchaViewModel

    /**
     * Handle signup with captcha
     */
    internal fun signupWithCaptcha() {
        viewModel.handleSignupWithCaptcha(context, getHeaderPayload(), signupData, siteKey)
    }

    /**
     * Get view model instance
     */
    internal val getViewModelInstance: SignupWithCaptchaViewModel
        get() = viewModel

    init {
        // Initialize SignupWithCaptchaViewModel
        val appContext: AppCompatActivity = context as AppCompatActivity
        val cyberArkAuthService: CyberArkAuthService =
            CyberArkAuthBuilder.getRetrofit(account.getBaseUrl)
                .create(CyberArkAuthService::class.java)
        viewModel = ViewModelProvider(
            appContext,
            CyberArkViewModelFactory(CyberArkAuthHelper(cyberArkAuthService))
        )[SignupWithCaptchaViewModel::class.java]
    }

    /**
     * Get header payload
     *
     * @return JSONObject
     */
    private fun getHeaderPayload(): JSONObject {
        val payload = JSONObject()
        payload.put(EndpointUrls.HEADER_X_IDAP_NATIVE_CLIENT, true)
        return payload
    }
}