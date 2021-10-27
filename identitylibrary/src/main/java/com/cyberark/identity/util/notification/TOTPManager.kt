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

import android.util.Base64
import com.cyberark.identity.data.model.OTPEnrollModel

/**
 * TOTP generator class used to generate OTP code using secret, algorithms, digits and period
 *
 */
internal object TOTPManager {

    /**
     * Generate time-based OTP code
     *
     * @param otpEnrollModel
     * @return
     */
    fun generateTOTP(otpEnrollModel: OTPEnrollModel): String {
        return try {
            val totp = TOTPGenerator(
                getTOTPAlgorithm(otpEnrollModel.Result.HmacAlgorithm),
                getSecretKeyByteArray(otpEnrollModel.Result.SecretKey),
                otpEnrollModel.Result.Digits,
                otpEnrollModel.Result.Period
            )
            totp.generate()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Secret is not a valid Base64 encoded TOTP secret", e
            )
        } catch (e: Exception) {
            throw Exception(
                "Exception in generating TOTP", e
            )
        }
    }

    /**
     * Get TOTP algorithm name
     *
     * @param code: algorithm code
     * @return String: algorithm name
     */
    private fun getTOTPAlgorithm(code: Int): String {
        return when(code) {
            0 -> TOTPAlgorithms.SHA1.algorithmName
            1 -> TOTPAlgorithms.SHA256.algorithmName
            2 -> TOTPAlgorithms.SHA512.algorithmName
            3 -> TOTPAlgorithms.MD5.algorithmName
            else -> {
                print("Invalid TOTP algorithms")
                ""
            }
        }
    }

    /**
     * Get secret key byte array
     *
     * @param secretKey: otp secret key
     * @return ByteArray
     */
    private fun getSecretKeyByteArray(secretKey: String): ByteArray? {
        val data: ByteArray = secretKey.toByteArray(charset("UTF-8"))
        val base64String = Base64.encodeToString(data, Base64.DEFAULT)
        return Base64.decode(base64String, Base64.DEFAULT)
    }
}