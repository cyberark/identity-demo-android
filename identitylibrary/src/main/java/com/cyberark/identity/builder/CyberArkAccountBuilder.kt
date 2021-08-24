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

package com.cyberark.identity.builder

import com.cyberark.identity.util.pkce.PKCEHelper
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.*

/**
 * CyberArk account builder
 *
 * @property domainURL: domain URL
 * @property clientId: client ID
 * @property appId: application ID
 * @property responseType: response Type
 * @property state: application state
 * @property scope: application access scope
 * @property redirectUri: client callback URI
 */
class CyberArkAccountBuilder(
        val domainURL: String?,
        val clientId: String?,
        val appId: String?,
        val responseType: String?,
        val state: String?,
        val scope: String?,
        val redirectUri: String?
) {

    private val baseURL: HttpUrl?
    private var codeVerifier: String? = null
    private var codeChallenge: String? = null

    companion object {
        const val KEY_RESPONSE_TYPE = "response_type"
        const val KEY_GRANT_TYPE = "grant_type"
        const val KEY_CODE = "code"
        const val KEY_REDIRECT_URI = "redirect_uri"
        const val KEY_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri"
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_SCOPE = "scope"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_CODE_VERIFIER = "code_verifier"
        const val KEY_CODE_CHALLENGE = "code_challenge"
        const val KEY_CODE_CHALLENGE_METHOD = "code_challenge_method"
        const val CODE_CHALLENGE_METHOD_VALUE = "S256"
        const val AUTHORIZATION_CODE_VALUE = "authorization_code"
    }

    /**
     * Builder data class
     *
     * @property domainURL: domain URL
     * @property clientId: client ID
     * @property appId: application ID
     * @property responseType: response Type
     * @property state: application state
     * @property scope: application scope
     * @property redirectUri: client callback URI
     */
    data class Builder(
            var domainURL: String? = null,
            var clientId: String? = null,
            var appId: String? = null,
            var responseType: String? = null,
            var state: String? = null,
            var scope: String? = null,
            var redirectUri: String? = null
    ) {

        /**
         * Set Domain URL
         *
         * @param domainURL
         */
        fun domainURL(domainURL: String) = apply { this.domainURL = domainURL }

        /**
         * Set Client ID
         *
         * @param clientId
         */
        fun clientId(clientId: String) = apply { this.clientId = clientId }

        /**
         * Set Application ID
         *
         * @param appId
         */
        fun appId(appId: String) = apply { this.appId = appId }

        /**
         * Set Response Type
         *
         * @param responseType
         */
        fun responseType(responseType: String) = apply { this.responseType = responseType }

        /**
         * Set Application State
         *
         * @param state
         */
        fun state(state: String) = apply { this.state = state }

        /**
         * Set Application Scope
         *
         * @param scope
         */
        fun scope(scope: String) = apply { this.scope = scope }

        /**
         * Set Redirect URI
         *
         * @param redirectUri
         */
        fun redirectUri(redirectUri: String) = apply { this.redirectUri = redirectUri }

        /**
         * Create CyberArk Account Builder
         *
         */
        fun build() = CyberArkAccountBuilder(
                domainURL,
                clientId,
                appId,
                responseType,
                state,
                scope,
                redirectUri
        )
    }

    /**
     * Get OAuth base URL
     */
    val OAuthBaseURL: String
        get() = baseURL!!.newBuilder()
                .addPathSegment("oauth2")
                .addPathSegment("authorize")
                .addPathSegment(appId.toString())
                .addQueryParameter(KEY_RESPONSE_TYPE, responseType.toString())
                .addQueryParameter(KEY_CLIENT_ID, clientId.toString())
                .addQueryParameter(KEY_SCOPE, scope.toString())
                .addQueryParameter(KEY_CODE_CHALLENGE, getCodeChallenge)
                .addQueryParameter(KEY_CODE_CHALLENGE_METHOD, CODE_CHALLENGE_METHOD_VALUE)
                .addEncodedQueryParameter(KEY_REDIRECT_URI, redirectUri.toString())
                .build()
                .toString()

    /**
     * Get OAuth end session URL
     */
    val OAuthEndSessionURL: String
        get() = baseURL!!.newBuilder()
                .addPathSegment("oauth2")
                .addPathSegment("endsession")
                .addEncodedQueryParameter(KEY_POST_LOGOUT_REDIRECT_URI, redirectUri.toString())
                .build()
                .toString()

    /**
     * Get application callback URL
     */
    val getRedirectURL: String
        get() = redirectUri.toString()

    /**
     * Get client ID
     */
    val getClientId: String
        get() = clientId.toString()

    /**
     * Get Base URL
     */
    val getBaseUrl: String
        get() = baseURL!!.newBuilder().toString()

    /**
     * Check valid and secure url
     *
     * @param url: authorize URL
     * @return HttpUrl
     */
    private fun checkValidUrl(url: String?): HttpUrl? {
        if (url == null) {
            return null
        }
        val validateURl = url.lowercase(Locale.ROOT)
        require(!validateURl.startsWith("http://")) { "Invalid url: '$url'." }
        val secureURL = if (validateURl.startsWith("https://")) validateURl else "https://$validateURl"
        return secureURL.toHttpUrlOrNull()
    }

    init {
        // Validate base URL
        baseURL = checkValidUrl(domainURL)
        requireNotNull(baseURL) { String.format("Invalid url: '%s'", domainURL) }

        // Get PKCE helper instance and generate code verifier and code challenge
        val pkceHelper = PKCEHelper()
        val codeVerifier: String = pkceHelper.generateCodeVerifier()
        val codeChallenge: String = pkceHelper.generateCodeChallenge(codeVerifier)

        // Assign to variables
        codeVerifier.also { this.codeVerifier = it }
        codeChallenge.also { this.codeChallenge = it }
    }

    /**
     * Get code challenge
     */
    private val getCodeChallenge: String
        get() = codeChallenge.toString()

    /**
     * Get code verifier
     */
    val getCodeVerifier: String
        get() = codeVerifier.toString()
}