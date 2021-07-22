package com.cyberark.identity.util.biometric

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.util.AlertDialogButtonCallback

interface BiometricPromptUtility {
    fun showBioAuthentication(
        activity: AppCompatActivity,
        retries: Int?,
        negitiveButtonText: String?,
        useDevicePin: Boolean = false
    )
}