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

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.cyberark.identity.R
import com.cyberark.identity.util.AlertDialogHandler

internal class BiometricPromptUtilityImpl(private val authenticationCallback: BiometricAuthenticationCallback) :
    BiometricPromptUtility {
    private val TAG = "BiometricPromptUtility"
    private var mMaxRetries = 3
    private var mFailedTries = 0

    private var isAutoCancelElabled = true
    private var mPrompt: BiometricPrompt? = null
    private var negitiveButtonText: String? = null
    private val securityPin = "1234"
    private lateinit var enrollFingerPrintDlg: AlertDialogHandler
    private var useDevicePin: Boolean = false

    private fun createBioAuthetication(
        activity: AppCompatActivity
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        mPrompt = getBioMetricPrompt(activity, executor, getBioMetricCallback())
//        mPrompt = BiometricPrompt(activity, executor, getBioMetricCallback())
        return mPrompt!!
    }

    private fun getBioMetricCallback() = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errCode, errString)
            if (errCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                this@BiometricPromptUtilityImpl.authenticationCallback.passwordAuthenticationSelected()
                mPrompt!!.cancelAuthentication()
            } else {
                this@BiometricPromptUtilityImpl.authenticationCallback.showErrorMessage(errString.toString())
            }
            mPrompt = null
            Log.d(TAG, "errCode is $errCode and errString is: $errString")
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            if (isAutoCancelElabled) {
                if (mFailedTries < mMaxRetries) {
                    mFailedTries++
                } else {
                    //If same object used multiple times by client
                    mFailedTries = 0
                    mPrompt?.cancelAuthentication()
                    this@BiometricPromptUtilityImpl.authenticationCallback.isAuthenticationSuccess(
                        false
                    )
                }
            }
            mPrompt = null
            Log.d(TAG, "User biometric rejected.")
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Log.d(TAG, "Authentication was successful")
            decryptServerTokenFromStorage(result)
            mPrompt = null
        }
    }

    private fun getBioMetricPrompt(
        activity: AppCompatActivity,
        executor: java.util.concurrent.Executor,
        callback: BiometricPrompt.AuthenticationCallback
    ) = BiometricPrompt(activity, executor, callback)

    override fun showBioAuthentication(
        activity: AppCompatActivity,
        retries: Int?,
        negitiveButtonText: String?,
        useDevicePin: Boolean
    ) {
        if (mPrompt != null) {
            return
        }
        if (retries == null || retries == 0) {
            isAutoCancelElabled = false
        }
        this.useDevicePin = useDevicePin
        this.negitiveButtonText = negitiveButtonText
        checkAndAuthenticate(activity)

    }


    private fun showBiometricPrompt(activity: AppCompatActivity) {
        if (mPrompt != null) {
            return
        }
        val promptInfo = this.createPromptInfo(activity)
        this.createBioAuthetication(activity)
            .authenticate(promptInfo)
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        Log.v(TAG, "auth result :: $authResult")
        this@BiometricPromptUtilityImpl.authenticationCallback.isAuthenticationSuccess(true)
    }

    private fun checkAndAuthenticate(activity: AppCompatActivity) {
        val biometricManager = getBioMetric(activity)
        println("Biometric manager $biometricManager")
        when (biometricManager.canAuthenticate(getSecurityType())) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt(activity)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                this.authenticationCallback.isHardwareSupported(false)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                this.authenticationCallback.isHardwareSupported(false)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                this.authenticationCallback.isBiometricEnrolled(false)
//                    if (activity != null) {
//                        showFingerEnrollmentAlert(activity!!)
//                    }
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                this.authenticationCallback.isHardwareSupported(false)
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                this.authenticationCallback.biometricErrorSecurityUpdateRequired()
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                this.authenticationCallback.isHardwareSupported(false)
            }
        }
    }

    private fun getBioMetric(activity: AppCompatActivity): BiometricManager =
        BiometricManager.from(activity)

//    override fun showFingerEnrollmentAlert(activity: Activity,callback: AlertDialogButtonCallback) {
//        enrollFingerPrintDlg = AlertDialogHandler(callback)
////        enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
////            override fun tappedButtonwithType(buttonType: AlertButtonType) {
////                launchBiometricSetup(activity)
////            }
////        })
//        enrollFingerPrintDlg.displayAlert(
//            activity,
//            activity.getString(R.string.cyberArkTitle),
//            activity.getString(R.string.biometricDescription), false,
//            mutableListOf<AlertButton>(AlertButton("OK", AlertButtonType.POSITIVE))
//        )
//    }

    fun dismissFingerPrintEnroll() {
        if (::enrollFingerPrintDlg.isInitialized) enrollFingerPrintDlg.dismissForcefully()
    }

    private fun launchBiometricSetup(activity: Activity?) {
        activity?.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }

    private fun getSecurityType(): Int {
        if (useDevicePin) {
            return BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }
        return BiometricManager.Authenticators.BIOMETRIC_STRONG
    }


    private fun createPromptInfo(activity: AppCompatActivity): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.getString(R.string.dialog_biometric_prompt_title))
//            setSubtitle(activity.getString(R.string.biometricpromptTitle))
            setDescription(activity.getString(R.string.dialog_biometric_prompt_desc))
            setConfirmationRequired(false)
            setAllowedAuthenticators(getSecurityType())
            if (useDevicePin == false) {
                setAllowedAuthenticators(getSecurityType())
                setNegativeButtonText(negitiveButtonText!!)
                negitiveButtonText = null
            }
        }.build()


}