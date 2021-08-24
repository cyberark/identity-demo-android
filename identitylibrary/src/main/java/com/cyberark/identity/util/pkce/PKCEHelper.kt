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
 * PKCE helper class is used to generate code challenge and code verifier
 *
 */
class PKCEHelper {

    /**
     * Generate code verifier using java SecureRandom
     *
     * @return String: base64 encoded string
     */
    fun generateCodeVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        return getBase64(code)
    }

    /**
     * Generate code challenge using code verifier
     *
     * @param codeVerifier: code verifier string
     * @return base64 encoded string
     */
    fun generateCodeChallenge(codeVerifier: String): String {
        val input = getASCII(codeVerifier)
        val signature = getSHA256Signature(input)
        return getBase64(signature)
    }

    /**
     * Get base64 string
     *
     * @param source: Byte Array
     * @return String: base64 encoded string
     */
    private fun getBase64(source: ByteArray): String {
        return Base64.encodeToString(source, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Get ASCII bytes
     *
     * @param value: string
     * @return ByteArray
     */
    private fun getASCII(value: String): ByteArray {
        return value.toByteArray(StandardCharsets.US_ASCII)
    }

    /**
     * Get SHA256 signature
     *
     * @param input: Byte Array
     * @return ByteArray: SHA256 signature in Byte Array
     */
    private fun getSHA256Signature(input: ByteArray): ByteArray {
        return try {
            val md = MessageDigest.getInstance(SHA_256)
            md.update(input, 0, input.size)
            md.digest()
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "Unable to get SHA256 signature", e)
            throw IllegalStateException("Unable to get SHA256 signature", e)
        }
    }

    companion object {
        private val TAG = PKCEHelper::class.java.simpleName
        private const val SHA_256 = "SHA-256"
    }
}