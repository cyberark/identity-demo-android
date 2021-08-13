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

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * JWT decoder utils
 *
 * @constructor Create empty JWT utils
 */
object JWTUtils {
    private val tag: String? = JWTUtils::class.simpleName
    fun isAccessTokenExpired(JWTEncoded: String): Boolean {
        var result: Boolean
        try {
            val split = JWTEncoded.split(".").toTypedArray()
            Log.d(tag, "JWT_DECODED Header: " + getJson(split[0]))
            Log.d(tag, "JWT_DECODED Body: " + getJson(split[1]))

            val jsonBody = JSONObject(getJson(split[1]))
            val exp: Long = jsonBody.getLong("exp")
            Log.d(tag, "JWT_DECODED exp: $exp")

            val ldt: LocalDateTime = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Instant.ofEpochMilli(exp * 1000L)
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            Log.d(tag, "LocalDateTime: $ldt")

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
     * Get json
     *
     * @param strEncoded
     * @return
     */
    private fun getJson(strEncoded: String): String {
        val charset = charset("UTF-8")
        val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, charset)
    }
}