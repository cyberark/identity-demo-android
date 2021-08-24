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

package com.cyberark.identity.util.biometric

import androidx.appcompat.app.AppCompatActivity

/**
 * CyberArk Biometric prompt utility
 *
 */
interface CyberArkBiometricPromptUtility {
    /**
     * Show biometrics authentication
     *
     * @param activity: Activity instance
     * @param retries: no of attempts
     * @param negitiveButtonText: negative button text
     * @param useDevicePin: user device pin flag, true/false
     */
    fun showBioAuthentication(
        activity: AppCompatActivity,
        retries: Int?,
        negitiveButtonText: String?,
        useDevicePin: Boolean = false
    )
}