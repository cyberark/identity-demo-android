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
import com.cyberark.identity.util.AlertButton
import com.cyberark.identity.util.AlertButtonType
import com.cyberark.identity.util.AlertDialogButtonCallback
import com.cyberark.identity.util.AlertDialogHandler


interface BiometricAuthenticationCallback {
    public fun isAuthenticationSuccess(success:Boolean)
    public fun passwordAuthenticationSelected()
    public fun showErrorMessage(message:String)
    public fun isHardwareSupported(boolean: Boolean)
    public fun isSdkVersionSupported(boolean: Boolean)
    public fun isBiometricEnrolled(boolean: Boolean)
    public fun biometricErrorSecurityUpdateRequired()
}

open class BiometricPromptUtility(authenticationCallback:BiometricAuthenticationCallback) {
    private val TAG = "BiometricPromptUtility"
    private var mMaxRetries = 3
    private var mFailedTries = 0

    private var isAutoCancelElabled = true
    private var mPrompt:BiometricPrompt? = null
    private val authenticationCallback:BiometricAuthenticationCallback = authenticationCallback
    private var negitiveButtonText:String? = null
    private val securityPin = "1234"
    private lateinit var enrollFingerPrintDlg: AlertDialogHandler
//    private var useDevicePin:Boolean = false

    private fun createBioAuthetication(activity: AppCompatActivity,succss:(BiometricPrompt.AuthenticationResult) -> Unit):BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                if (errCode == 13) {
//                    this@BiometricPromptUtility.authenticationCallback.passwordAuthenticationSelected()
//                    val pinIntent = Intent(activity.applicationContext,SecurityPinActivity::class.java).apply{
//                        putExtra("securitypin",securityPin)
//                    }
//                    activity.startActivity(pinIntent)
                }else {
                    this@BiometricPromptUtility.authenticationCallback.showErrorMessage(errString.toString())
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
                        this@BiometricPromptUtility.authenticationCallback.isAuthenticationSuccess(false)
                    }
                }
                mPrompt = null
                Log.d(TAG, "User biometric rejected.")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                succss(result)
                mPrompt = null
            }
        }
        mPrompt = BiometricPrompt(activity, executor, callback)
        return mPrompt!!
    }

    fun showBioAuthentication(activity: AppCompatActivity,retries:Int?,negitiveButtonText:String?
                                     ,useDevicePin:Boolean = false) {
        if (mPrompt != null) {
            return
        }
        if (retries == null || retries == 0) {
            isAutoCancelElabled = false
        }
//        this.useDevicePin = useDevicePin
        this.negitiveButtonText = negitiveButtonText
        checkAndAuthenticate(activity)

    }



    private fun showBiometricPrompt(activity: AppCompatActivity) {
        if (mPrompt != null) {
            return
        }
        val promptInfo = this.createPromptInfo(activity)
        this.createBioAuthetication(activity,::decryptServerTokenFromStorage).authenticate(promptInfo)
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        Log.v(TAG,"authresult ")
        this@BiometricPromptUtility.authenticationCallback.isAuthenticationSuccess(true)
    }

    private fun checkAndAuthenticate(activity: AppCompatActivity) {
        val biometricManager = BiometricManager.from(activity)
        println("Biometric manager $biometricManager")
        if (biometricManager != null) {
            println("Biometri can authenticate ${biometricManager.canAuthenticate(getSecurityType())}")
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
                    if (activity != null) {
                        showFingerEnrollmentAlert(activity!!)
                    }
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
    }

    private fun showFingerEnrollmentAlert(activity: Activity) {
        enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonwithType(buttonType: AlertButtonType) {
                launchBiometricSetup(activity)
            }
        })
        enrollFingerPrintDlg.displayAlert(activity,activity.getString(R.string.cyberArkTitle),activity.getString(R.string.biometricDescription), mutableListOf<AlertButton>(AlertButton("OK",AlertButtonType.POSITIVE)))
    }



    fun dismissFingerPrintEnroll() {
        if (enrollFingerPrintDlg != null) {
            enrollFingerPrintDlg.dismissAlert()
        }
    }

    private fun launchBiometricSetup(activity: Activity?) {
        if (activity != null) {
            activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
    }

    private fun getSecurityType() : Int =
          BiometricManager.Authenticators.BIOMETRIC_STRONG


    private fun createPromptInfo(activity: AppCompatActivity): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.getString(R.string.cyberArkTitle))
            setSubtitle(activity.getString(R.string.biometricTitle))
            setDescription(activity.getString(R.string.biometricDescription))
            setConfirmationRequired(false)
            setAllowedAuthenticators(getSecurityType())
            if (negitiveButtonText != null) {
                setNegativeButtonText(negitiveButtonText!!)
                negitiveButtonText = null
            }
//            if (useDevicePin == false) {
//
//            }else {
//                setDeviceCredentialAllowed(true)
//            }
        }.build()


}