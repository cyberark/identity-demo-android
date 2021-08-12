package com.cyberark.identity.util.jwt

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

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

    private fun getJson(strEncoded: String): String {
        val charset = charset("UTF-8")
        val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, charset)
    }
}