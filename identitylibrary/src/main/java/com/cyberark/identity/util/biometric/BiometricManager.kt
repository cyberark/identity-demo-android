package com.cyberark.identity.util.biometric

class BiometricManager {
    fun getBiometricUtility(callback: BiometricAuthenticationCallback):BiometricPromptUtility {
        val biometricPromptUtility:BiometricPromptUtility =  BiometricPromptUtilityImpl(callback)
        return biometricPromptUtility
    }
}