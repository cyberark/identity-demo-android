package com.cyberark.identity.util.biometric

import androidx.appcompat.app.AppCompatActivity

interface BiometricPromptUtility {
    fun showBioAuthentication(
        activity: AppCompatActivity,
        retries: Int?,
        negitiveButtonText: String?,
        useDevicePin: Boolean = false
    )
}