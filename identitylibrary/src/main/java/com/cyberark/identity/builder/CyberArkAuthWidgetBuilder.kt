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

package com.cyberark.identity.builder

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URLDecoder
import java.util.*

/**
 * CyberArk authentication widget builder
 *
 * @property systemURL: system URL
 * @property hostURL: host URL
 * @property widgetId: widget ID
 */
class CyberArkAuthWidgetBuilder(
    private val systemURL: String?,
    private val hostURL: String?,
    private val widgetId: String?
) {
    private val baseSystemURL: HttpUrl?
    private val baseURL: HttpUrl?

    companion object {
        const val KEY_WIDGET_ID = "id"
    }

    /**
     * Builder data class
     *
     * @property systemURL: system URL
     * @property hostURL: host URL
     * @property widgetId: widget ID
     */
    data class Builder(
        var systemURL: String? = null,
        var hostURL: String? = null,
        var widgetId: String? = null
    ) {

        /**
         * Set System URL
         *
         * @param systemURL
         */
        fun systemURL(systemURL: String) = apply { this.systemURL = systemURL }

        /**
         * Set host URL
         *
         * @param hostURL
         */
        fun hostURL(hostURL: String) = apply { this.hostURL = hostURL }

        /**
         * Set Widget ID
         *
         * @param widgetId
         */
        fun widgetId(widgetId: String) = apply { this.widgetId = widgetId }

        /**
         * Create CyberArk widget Builder
         *
         */
        fun build() = CyberArkAuthWidgetBuilder(
            systemURL,
            hostURL,
            widgetId
        )
    }

    /**
     * Get MFA Widget base URL
     */
    fun getAuthWidgetBaseURL(username: String): String {
        val widgetURL = baseURL!!.newBuilder()
            .addPathSegment("Authenticationwidgets")
            .addPathSegment("WidgetPage")
            .addQueryParameter(KEY_WIDGET_ID, widgetId)
            .build()
            .toString()
        return URLDecoder.decode(widgetURL, "UTF-8")
    }

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
        val secureURL =
            if (validateURl.startsWith("https://")) validateURl else "https://$validateURl"
        return secureURL.toHttpUrlOrNull()
    }

    init {
        // Validate system URL
        baseSystemURL = checkValidUrl(systemURL)
        requireNotNull(baseSystemURL) { String.format("Invalid url: '%s'", systemURL) }

        // Validate base URL
        baseURL = checkValidUrl(hostURL)
        requireNotNull(baseURL) { String.format("Invalid url: '%s'", hostURL) }
    }
}