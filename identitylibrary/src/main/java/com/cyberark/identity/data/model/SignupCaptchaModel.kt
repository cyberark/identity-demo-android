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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * OTP enroll model class
 *
 * @property success: OTP enroll status true/false
 * @property Result: Signup captcha result
 * @property Message: message
 * @property MessageID: message ID
 * @property Exception: OTP enroll exception
 * @property ErrorID: error ID
 * @property ErrorCode: error code
 * @property IsSoftError: soft error, true/false
 * @property InnerExceptions: inner exceptions
 *
 */
@Parcelize
data class SignupCaptchaModel(
    val success: Boolean,
    val Result: SignupCaptchaResult,
    val Message: String,
    val MessageID: String,
    val Exception: String,
    val ErrorID: String,
    val ErrorCode: String,
    val IsSoftError: Boolean,
    val InnerExceptions: String
) : Parcelable
