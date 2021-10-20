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

package com.cyberark.identity.data.model

/**
 * OTP enroll result class
 *
 * @property Status: 0 for success, 1 for failure
 * @property OTPKey: OTP key
 * @property OTPKeyVersion: OTP key version
 * @property OTPCodeMinLength: OTP code minimum length
 * @property OTPCodeExpiryInterval: OTP code expiry interval
 * @property OathProfileUuid: oauth profile uuid
 * @property OathType: oauth type
 * @property AccountName: account name
 * @property Issuer: issuer name
 * @property SecretKey: secret key
 * @property Period: period value
 * @property Digits: digits value
 * @property Counter: counter value
 * @property HmacAlgorithm: HMAC algorithms value
 * @property SecretVersion: secret version
 * @property IsCma: cma status true/false
 *
 */
data class OTPEnrollResult(
    val Status: Int,
    val OTPKey: String,
    val OTPKeyVersion: Int,
    val OTPCodeMinLength: Int,
    val OTPCodeExpiryInterval: Int,
    val OathProfileUuid: String,
    val OathType: Int,
    val AccountName: String,
    val Issuer: String,
    val SecretKey: String,
    val Period: Int,
    val Digits: Int,
    val Counter: Int,
    val HmacAlgorithm: Int,
    val SecretVersion: Int,
    val IsCma: Boolean
)
