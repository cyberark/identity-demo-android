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

package com.cyberark.identity.data.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(OTPEnrollModel::class, OTPEnrollResult::class)
class OTPEnrollModelTest {

    private var otpEnrollModel: OTPEnrollModel? = null
    private var otpEnrollResult: OTPEnrollResult? = null

    @Before
    fun setUp() {
        otpEnrollResult = OTPEnrollResult(
            1,
            "OTP Key",
            2,
            10,
            60,
            "OAuth Profile UUID",
            1,
            "Acme",
            "Acme Inc",
            "Secret Key",
            180,
            5,
            1,
            10,
            11,
            true
        )
        otpEnrollModel = OTPEnrollModel(
            true,
            otpEnrollResult!!,
            "Message",
            "Message ID",
            "Exception",
            "Error ID",
            "Error Code",
            false,
            "Inner Exceptions"
        )
    }

    @Test
    fun getStatus() {
        assertThat(otpEnrollResult?.Status, `is`(1))
    }

    @Test
    fun getOTPKey() {
        assertThat(otpEnrollResult?.OTPKey, `is`("OTP Key"))
    }

    @Test
    fun getOTPKeyVersion() {
        assertThat(otpEnrollResult?.OTPKeyVersion, `is`(2))
    }

    @Test
    fun getOTPCodeMinLength() {
        assertThat(otpEnrollResult?.OTPCodeMinLength, `is`(10))
    }

    @Test
    fun getOTPCodeExpireInterval() {
        assertThat(otpEnrollResult?.OTPCodeExpiryInterval, `is`(60))
    }

    @Test
    fun getOauthProfileUUID() {
        assertThat(otpEnrollResult?.OathProfileUuid, `is`("OAuth Profile UUID"))
    }

    @Test
    fun getOauthType() {
        assertThat(otpEnrollResult?.OathType, `is`(1))
    }

    @Test
    fun getAccountName() {
        assertThat(otpEnrollResult?.AccountName, `is`("Acme"))
    }

    @Test
    fun getIssuer() {
        assertThat(otpEnrollResult?.Issuer, `is`("Acme Inc"))
    }

    @Test
    fun getSecretKey() {
        assertThat(otpEnrollResult?.SecretKey, `is`("Secret Key"))
    }

    @Test
    fun getPeriod() {
        assertThat(otpEnrollResult?.Period, `is`(180))
    }

    @Test
    fun getDigits() {
        assertThat(otpEnrollResult?.Digits, `is`(5))
    }

    @Test
    fun getCounter() {
        assertThat(otpEnrollResult?.Counter, `is`(1))
    }

    @Test
    fun getHmacAlg() {
        assertThat(otpEnrollResult?.HmacAlgorithm, `is`(10))
    }

    @Test
    fun getSecretVersion() {
        assertThat(otpEnrollResult?.SecretVersion, `is`(11))
    }

    @Test
    fun isCma() {
        assertThat(otpEnrollResult?.IsCma, `is`(true))
    }

    @Test
    fun getSuccess() {
        assertThat(otpEnrollModel?.success, `is`(true))
    }

    @Test
    fun getMessage() {
        assertThat(otpEnrollModel?.Message, `is`("Message"))
    }

    @Test
    fun getMessageId() {
        assertThat(otpEnrollModel?.MessageID, `is`("Message ID"))
    }

    @Test
    fun getException() {
        assertThat(otpEnrollModel?.Exception, `is`("Exception"))
    }

    @Test
    fun getErrorId() {
        assertThat(otpEnrollModel?.ErrorID, `is`("Error ID"))
    }

    @Test
    fun getErrorCode() {
        assertThat(otpEnrollModel?.ErrorCode, `is`("Error Code"))
    }

    @Test
    fun isSoftError() {
        assertThat(otpEnrollModel?.IsSoftError, `is`(false))
    }

    @Test
    fun getInnerExceptions() {
        assertThat(otpEnrollModel?.InnerExceptions, `is`("Inner Exceptions"))
    }
}