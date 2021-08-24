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

package com.cyberark.identity.util.jwt

import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.time.Instant

/**
 * JWT decoder util is used to validate access token
 *
 */
object JWTUtils {
    private val tag: String? = JWTUtils::class.simpleName

    /**
     * Is access token expired
     *
     * @param JWTEncoded: access token data
     * @return Boolean
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isAccessTokenExpired(JWTEncoded: String): Boolean {
        var result: Boolean
        try {
            val split = JWTEncoded.split(".").toTypedArray()
            val jsonBody = JSONObject(getJson(split[1]))
            val exp: Long = jsonBody.getLong("exp")

            val currentTime = Instant.now().toEpochMilli()
            val expireTime = exp * 1000L

            Log.d(tag, "currentTime: $currentTime")
            Log.d(tag, "System currentTime: " + System.currentTimeMillis())

            result = currentTime <= expireTime

        } catch (e: UnsupportedEncodingException) {
            result = false
            Log.i(tag, "UnsupportedEncodingException Error parsing JWT: $e")
        } catch (e: Exception) {
            result = false
            Log.i(tag, "Exception Error parsing JWT: $e")
        }
        return result
    }

    /**
     * Get Json string
     *
     * @param strEncoded: encoded string
     * @return decoded string
     */
    private fun getJson(strEncoded: String): String {
        val charset = charset("UTF-8")
        val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, charset)
    }
}