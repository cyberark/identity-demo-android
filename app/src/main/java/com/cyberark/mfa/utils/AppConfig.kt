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
import com.cyberark.identity.builder.CyberArkAuthWidgetBuilder
import com.cyberark.identity.builder.CyberArkMFAWidgetBuilder
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.BuildConfig
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
            .systemURL(context.getString(R.string.cyberark_auth_system_url))
            .hostURL(context.getString(R.string.cyberark_auth_host_url))
            .clientId(context.getString(R.string.cyberark_auth_client_id))
            .appId(context.getString(R.string.cyberark_auth_app_id))
            .responseType(context.getString(R.string.cyberark_auth_response_type))
            .scope(context.getString(R.string.cyberark_auth_scope))
            .redirectUri(context.getString(R.string.cyberark_auth_redirect_uri))
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
            if(BuildConfig.DEBUG) {
                saveLocalConfigInSharedPreference(context)
            } else {
                saveConfigInSharedPreference(context)
            }
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
            .responseType(context.getString(R.string.cyberark_auth_response_type))
            .scope(context.getString(R.string.cyberark_auth_scope))
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
            context.getString(R.string.cyberark_auth_system_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.HOST_URL,
            context.getString(R.string.cyberark_auth_host_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.CLIENT_ID,
            context.getString(R.string.cyberark_auth_client_id)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.APP_ID,
            context.getString(R.string.cyberark_auth_app_id)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.REDIRECT_URI,
            context.getString(R.string.cyberark_auth_redirect_uri)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.HOST,
            context.getString(R.string.cyberark_auth_host)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.SCHEME,
            context.getString(R.string.cyberark_auth_scheme)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.SITE_KEY,
            context.getString(R.string.recaptcha_v2_site_key)
        )
    }

    /**
     * Save local.properties config details in shared preference
     *
     * @param context: Activity/Application context
     */
    private fun saveLocalConfigInSharedPreference(context: Context) {
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.SYSTEM_URL,
            context.getString(R.string.config_auth_system_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.HOST_URL,
            context.getString(R.string.config_auth_host_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.CLIENT_ID,
            context.getString(R.string.config_auth_client_id)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.APP_ID,
            context.getString(R.string.config_auth_app_id)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.REDIRECT_URI,
            context.getString(R.string.config_auth_redirect_uri)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.HOST,
            context.getString(R.string.config_auth_host)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.SCHEME,
            context.getString(R.string.config_auth_scheme)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.SITE_KEY,
            context.getString(R.string.config_recaptcha_key)
        )
    }

    /**
     * Get Google reCaptcha V2 site key
     *
     * @param context: Activity/Application context
     * @return Native Login URL string
     */
    fun getSiteKey(context: Context): String {
        val siteKeySP = CyberArkPreferenceUtil.getString(PreferenceConstants.SITE_KEY, null)
        if (siteKeySP == null) {
            if(BuildConfig.DEBUG) {
                saveLocalConfigInSharedPreference(context)
            } else {
                saveConfigInSharedPreference(context)
            }
        }
        return CyberArkPreferenceUtil.getString(PreferenceConstants.SITE_KEY, null).toString()
    }

    /**
     * Set-up account for native login flow
     * update configuration from SharedPreference
     *
     * @param context: Activity/Application context
     * @return CyberArkWidgetBuilder instance
     */
    fun setupNativeLoginFromSharedPreference(context: Context): CyberArkMFAWidgetBuilder{
        val nativeLoginURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.NATIVE_LOGIN_URL, null)
        if (nativeLoginURLSP == null) {
            if(BuildConfig.DEBUG) {
                saveLocalNativeLoginURLInSharedPreference(context)
            } else {
                saveNativeLoginURLInSharedPreference(context)
            }
        }
        val systemUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.SYSTEM_URL, null)
        val widgetHostUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.MFA_WIDGET_URL, null)
        val widgetId = CyberArkPreferenceUtil.getString(PreferenceConstants.MFA_WIDGET_ID, null)

        return CyberArkMFAWidgetBuilder.Builder()
            .systemURL(systemUrl.toString())
            .hostURL(widgetHostUrl.toString())
            .widgetId(widgetId.toString())
            .build()
    }

    /**
     * Get native login URL
     *
     * @param context: Activity/Application context
     * @return Native Login URL string
     */
     fun getNativeLoginURL(context: Context): String? {
        val nativeLoginURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.NATIVE_LOGIN_URL, null)
        if (nativeLoginURLSP == null) {
            if(BuildConfig.DEBUG) {
                saveLocalNativeLoginURLInSharedPreference(context)
            } else {
                saveNativeLoginURLInSharedPreference(context)
            }
        }
        return CyberArkPreferenceUtil.getString(PreferenceConstants.NATIVE_LOGIN_URL, null)
    }

    /**
     * Save native login url, host url and widget id in shared preference
     *
     * @param context: Activity/Application context
     */
    private fun saveNativeLoginURLInSharedPreference(context: Context) {
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.NATIVE_LOGIN_URL,
            context.getString(R.string.acme_native_login_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.MFA_WIDGET_URL,
            context.getString(R.string.cyberark_mfa_widget_host_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.MFA_WIDGET_ID,
            context.getString(R.string.cyberark_mfa_widget_id)
        )
    }

    /**
     * Save local.properties native login url, host url and widget id in shared preference
     *
     * @param context: Activity/Application context
     */
    private fun saveLocalNativeLoginURLInSharedPreference(context: Context) {
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.NATIVE_LOGIN_URL,
            context.getString(R.string.config_native_login_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.MFA_WIDGET_URL,
            context.getString(R.string.config_mfa_widget_host_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.MFA_WIDGET_ID,
            context.getString(R.string.config_mfa_widget_id)
        )
    }

    /**
     * Set-up account for Authentication Widget
     * update configuration from SharedPreference
     *
     * @param context: Activity/Application context
     * @return CyberArkAuthWidgetBuilder instance
     */
    fun setupAuthWidgetFromSharedPreference(context: Context): CyberArkAuthWidgetBuilder{
        val authWidgetURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.AUTH_WIDGET_URL, null)
        if (authWidgetURLSP == null) {
            if(BuildConfig.DEBUG) {
                saveLocalAuthWidgetURLInSharedPreference(context)
            } else {
                saveAuthWidgetURLInSharedPreference(context)
            }
        }
        val systemUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.SYSTEM_URL, null)
        val widgetHostUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.AUTH_WIDGET_URL, null)
        val widgetId = CyberArkPreferenceUtil.getString(PreferenceConstants.AUTH_WIDGET_ID, null)
        val resourceUrl = CyberArkPreferenceUtil.getString(PreferenceConstants.RESOURCE_URL, null)

        return CyberArkAuthWidgetBuilder.Builder()
            .systemURL(systemUrl.toString())
            .hostURL(widgetHostUrl.toString())
            .widgetId(widgetId.toString())
            .resourceURL(resourceUrl.toString())
            .build()
    }

    /**
     * Save Authentication Widget host url and widget id in shared preference
     *
     * @param context: Activity/Application context
     */
    private fun saveAuthWidgetURLInSharedPreference(context: Context) {
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.AUTH_WIDGET_URL,
            context.getString(R.string.cyberark_auth_widget_host_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.AUTH_WIDGET_ID,
            context.getString(R.string.cyberark_auth_widget_id)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.RESOURCE_URL,
            context.getString(R.string.cyberark_auth_resource_url)
        )
    }

    /**
     * Save local.properties host url and widget id in shared preference
     *
     * @param context: Activity/Application context
     */
    private fun saveLocalAuthWidgetURLInSharedPreference(context: Context) {
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.AUTH_WIDGET_URL,
            context.getString(R.string.config_auth_widget_host_url)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.AUTH_WIDGET_ID,
            context.getString(R.string.config_auth_widget_id)
        )
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.RESOURCE_URL,
            context.getString(R.string.config_auth_resource_url)
        )
    }
}