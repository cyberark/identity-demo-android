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

package com.cyberark.mfa.utils

import android.content.Context
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R

object AppConfig {

    /**
     * Set-up account for OAuth 2.0 PKCE driven flow
     * update account configuration from "res/values/config.xml"
     *
     * @param context: Activity/Application context
     * @return cyberArkAccountBuilder: CyberArkAccountBuilder instance
     */
    fun setupAccount(context: Context): CyberArkAccountBuilder {
        return CyberArkAccountBuilder.Builder()
            .systemURL(context.getString(R.string.cyberark_account_system_url))
            .hostURL(context.getString(R.string.cyberark_account_host_url))
            .clientId(context.getString(R.string.cyberark_account_client_id))
            .appId(context.getString(R.string.cyberark_account_app_id))
            .responseType(context.getString(R.string.cyberark_account_response_type))
            .scope(context.getString(R.string.cyberark_account_scope))
            .redirectUri(context.getString(R.string.cyberark_account_redirect_uri))
            .build()
    }

    /**
     * Set-up account for OAuth 2.0 PKCE driven flow
     * update account configuration from SharedPreference
     *
     * @param context: Activity/Application context
     * @return cyberArkAccountBuilder: CyberArkAccountBuilder instance
     */
    fun setupAccountFromSharedPreference(context: Context): CyberArkAccountBuilder {
        val systemURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.SYSTEM_URL, null)
        if (systemURLSP == null) {
            saveConfigInSharedPreference(context)
        }
        val systemUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.SYSTEM_URL, null)
        val hostUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.HOST_URL, null)
        val clientId = CyberArkPreferenceUtil.getString(PreferenceConstants.CLIENT_ID, null)
        val appId = CyberArkPreferenceUtil.getString(PreferenceConstants.APP_ID, null)
        val redirectUri = CyberArkPreferenceUtil.getString(PreferenceConstants.REDIRECT_URI, null)

        return CyberArkAccountBuilder.Builder()
            .systemURL(systemUrl.toString())
            .hostURL(hostUrl.toString())
            .clientId(clientId.toString())
            .appId(appId.toString())
            .responseType(context.getString(R.string.cyberark_account_response_type))
            .scope(context.getString(R.string.cyberark_account_scope))
            .redirectUri(redirectUri.toString())
            .build()
    }

    /**
     * Save config details in shared preference
     *
     * @param context: Activity/Application context
     */
    private fun saveConfigInSharedPreference(context: Context) {
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.SYSTEM_URL,
            context.getString(R.string.cyberark_account_system_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.HOST_URL,
            context.getString(R.string.cyberark_account_host_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.CLIENT_ID,
            context.getString(R.string.cyberark_account_client_id)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.APP_ID,
            context.getString(R.string.cyberark_account_app_id)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.REDIRECT_URI,
            context.getString(R.string.cyberark_account_redirect_uri)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.HOST,
            context.getString(R.string.cyberark_account_host)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.SCHEME,
            context.getString(R.string.cyberark_account_scheme)
        )
    }

    /**
     * Set-up account for basic login flow
     * update configuration from SharedPreference
     *
     * @param context: Activity/Application context
     * @return cyberArkAccountBuilder: CyberArkAccountBuilder instance
     */
    fun setupBasicLoginFromSharedPreference(context: Context): CyberArkAccountBuilder {
        val basicLoginURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.BASIC_LOGIN_URL, null)
        if (basicLoginURLSP == null) {
            saveBasicLoginURLInSharedPreference(context)
        }
        val basicLoginUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.BASIC_LOGIN_URL, null)
        val systemUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.SYSTEM_URL, null)
        val hostUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.HOST_URL, null)

        return CyberArkAccountBuilder.Builder()
            .basicLoginURL(basicLoginUrl.toString())
            .systemURL(systemUrl.toString())
            .hostURL(hostUrl.toString())
            .build()
    }

    /**
     * Save basic login url in shared preference
     *
     * @param context: Activity/Application context
     */
    private fun saveBasicLoginURLInSharedPreference(context: Context) {
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.BASIC_LOGIN_URL,
            context.getString(R.string.cyberark_account_basic_login_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.SYSTEM_URL,
            context.getString(R.string.cyberark_account_system_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.HOST_URL,
            context.getString(R.string.cyberark_account_host_url)
        )
    }
}