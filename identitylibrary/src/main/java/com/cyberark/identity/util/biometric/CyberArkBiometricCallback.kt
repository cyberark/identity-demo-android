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

/**
 * CyberArk biometric authentication callback
 *
 */
interface CyberArkBiometricCallback {
    /**
     * Is authentication success
     *
     * @param success
     */
    fun isAuthenticationSuccess(success: Boolean)

    /**
     * Password authentication selected
     *
     */
    fun passwordAuthenticationSelected()

    /**
     * Show error message
     *
     * @param message
     */
    fun showErrorMessage(message: String)

    /**
     * Is hardware supported
     *
     * @param boolean
     */
    fun isHardwareSupported(boolean: Boolean)

    /**
     * Is sdk version supported
     *
     * @param boolean
     */
    fun isSdkVersionSupported(boolean: Boolean)

    /**
     * Is biometric enrolled
     *
     * @param boolean
     */
    fun isBiometricEnrolled(boolean: Boolean)

    /**
     * Biometric error security update required
     *
     */
    fun biometricErrorSecurityUpdateRequired()
}