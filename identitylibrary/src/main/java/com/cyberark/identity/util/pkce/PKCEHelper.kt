package com.cyberark.identity.util.pkce

import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class PKCEHelper {
    private fun getBase64String(source: ByteArray): String {
        return Base64.encodeToString(source, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun getASCIIBytes(value: String): ByteArray {
        return value.toByteArray(StandardCharsets.US_ASCII)
    }

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

    fun generateCodeVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        return Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

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