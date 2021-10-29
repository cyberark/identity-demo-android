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

package com.cyberark.identity.util.notification

import okhttp3.internal.and
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Generates time-based otp code using algorithm, secret, digits, period and system clock
 *
 * @constructor
 *
 * @param algorithm: supported algorithms
 * @param secret: base 64 secret key
 * @param digits: digit value
 * @param period: period value
 * @param clock: SystemClock
 */
internal class TOTPGenerator constructor(
    algorithm: String,
    secret: ByteArray?,
    digits: Int,
    period: Int,
    clock: SystemClock
) {
    private val digits: Int
    private val period: Int
    private val clock: SystemClock
    private var hmac: Mac? = null

    /**
     * TOTP class constructor
     *
     * @param algorithm: crypto algorithm
     * @param secret: byte array secret
     * @param digits: number of digits
     * @param period: time period (in seconds)
     */
    internal constructor(algorithm: String, secret: ByteArray?, digits: Int, period: Int) : this(
        algorithm,
        secret,
        digits,
        period,
        SystemClock()
    )

    /**
     * Generates the code corresponding to the current date and time
     *
     * @return String: OTP code
     */
    fun generate(): String {
        val timeSecs = clock.currentTimeSecs
        return generate(timeSecs / period)
    }

    /**
     * Generates the code for the specified counter value
     *
     * @param eventCount: counter value
     * @return String: OTP code
     */
    fun generate(eventCount: Long): String {
        // convert to byte array
        var movingFactor = eventCount
        val counter = ByteArray(8) // 64 bits
        for (i in counter.indices.reversed()) {
            counter[i] = (movingFactor and 0xff).toByte()
            movingFactor = movingFactor shr 8
        }

        // This method uses the JCE to provide the crypto.
        // HMAC computes a Hashed Message Authentication Code with the crypto hash as a parameter.
        val hash = hmac!!.doFinal(counter)

        // put selected bytes into result int
        val offset: Int = hash[hash.size - 1] and 0xf
        val binary: Int = hash[offset] and 0x7f shl 24 or
                (hash[offset + 1] and 0xff shl 16) or
                (hash[offset + 2] and 0xff shl 8) or
                (hash[offset + 3] and 0xff)
        val otp = binary % DIGITS_POWER[digits]
        var result = otp.toString()

        // padding with zeros to complete code length
        while (result.length < digits) {
            result = "0$result"
        }
        return result
    }

    internal class SystemClock {
        val currentTimeSecs: Long
            get() = System.currentTimeMillis() / 1000
    }

    companion object {
        private const val HMAC_SHA1 = "HmacSHA1"
        private const val HMAC_SHA256 = "HmacSHA256"
        private const val HMAC_SHA512 = "HmacSHA512"
        private const val HMAC_MD5 = "HmacMD5"
        private val DIGITS_POWER // 0  1   2    3     4      5       6        7         8
                = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)
    }

    init {
        hmac = try {
            when (algorithm.lowercase()) {
                "sha1" -> Mac.getInstance(HMAC_SHA1)
                "sha256" -> Mac.getInstance(HMAC_SHA256)
                "sha512" -> Mac.getInstance(HMAC_SHA512)
                "md5" -> Mac.getInstance(HMAC_MD5)
                else -> throw IllegalArgumentException("Unsupported algorithm: $algorithm")
            }
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException("The specified crypto algorithm is not available", e)
        }
        try {
            val macKey = SecretKeySpec(secret, "RAW")
            hmac!!.init(macKey)
        } catch (e: InvalidKeyException) {
            throw IllegalArgumentException("The mac key is not valid", e)
        }
        require(digits < DIGITS_POWER.size) { "Unsupported digits. It should not exceed 8 (was: $digits)" }
        this.digits = digits
        this.period = period
        this.clock = clock
    }
}