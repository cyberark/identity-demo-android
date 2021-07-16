package com.cyberark.identity.util.biometric

internal interface BiometricAuthenticationCallback {
    fun isAuthenticationSuccess(success: Boolean)
    fun passwordAuthenticationSelected()
    fun showErrorMessage(message: String)
    fun isHardwareSupported(boolean: Boolean)
    fun isSdkVersionSupported(boolean: Boolean)
    fun isBiometricEnrolled(boolean: Boolean)
    fun biometricErrorSecurityUpdateRequired()
}