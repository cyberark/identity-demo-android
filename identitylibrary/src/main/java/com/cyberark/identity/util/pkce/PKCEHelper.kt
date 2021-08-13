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

package com.cyberark.identity.util.pkce

import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

/**
 * PKCE helper
 *
 * @constructor Create empty PKCE helper
 */
class PKCEHelper {
    private fun getBase64String(source: ByteArray): String {
        return Base64.encodeToString(source, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Get ASCII bytes
     *
     * @param value
     * @return
     */
    fun getASCIIBytes(value: String): ByteArray {
        return value.toByteArray(StandardCharsets.US_ASCII)
    }

    /**
     * Get SHA256
     *
     * @param input
     * @return
     */
    fun getSHA256(input: ByteArray): ByteArray {
        val signature: ByteArray
        signature = try {
            val md = MessageDigest.getInstance(SHA_256)
            md.update(input, 0, input.size)
            md.digest()
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "Failed to get SHA-256 signature", e)
            throw IllegalStateException("Failed to get SHA-256 signature", e)
        }
        return signature
    }

    /**
     * Generate code verifier
     *
     * @return
     */
    fun generateCodeVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        return Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Generate code challenge
     *
     * @param codeVerifier
     * @return
     */
    fun generateCodeChallenge(codeVerifier: String): String {
        val input = getASCIIBytes(codeVerifier)
        val signature = getSHA256(input)
        return getBase64String(signature)
    }

    companion object {
        private val TAG = PKCEHelper::class.java.simpleName
        private const val SHA_256 = "SHA-256"
    }
}