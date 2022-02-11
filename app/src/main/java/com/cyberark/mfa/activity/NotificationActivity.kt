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

package com.cyberark.mfa.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.NotificationDataModel
import com.cyberark.identity.data.model.OTPEnrollModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.data.model.SubmitOTPModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.*
import com.cyberark.identity.util.biometric.CyberArkBiometricCallback
import com.cyberark.identity.util.biometric.CyberArkBiometricManager
import com.cyberark.identity.util.biometric.CyberArkBiometricPromptUtility
import com.cyberark.identity.util.dialog.AlertButton
import com.cyberark.identity.util.dialog.AlertButtonType
import com.cyberark.identity.util.dialog.AlertDialogButtonCallback
import com.cyberark.identity.util.dialog.AlertDialogHandler
import com.cyberark.identity.util.jwt.JWTUtils
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.notification.NotificationConstants
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.fcm.FCMReceiver
import com.cyberark.mfa.utils.AppConfig
import com.cyberark.mfa.utils.PreferenceConstants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Handle push Notification from activity
 *
 */
class NotificationActivity : AppCompatActivity() {

    companion object {
        private val TAG = NotificationActivity::class.simpleName
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var notificationDesc: TextView
    private lateinit var approveButton: ImageButton
    private lateinit var denyButton: ImageButton
    private lateinit var notificationData: NotificationDataModel
    private lateinit var otpEnrollModel: OTPEnrollModel

    private lateinit var accessTokenData: String
    private lateinit var refreshTokenData: String
    private var userAccepted: Boolean = false
    private var logoutStatus: Boolean = false
    private var tokenExpireStatus: Boolean = false

    // SDK biometrics utility variable
    private lateinit var cyberArkBiometricPromptUtility: CyberArkBiometricPromptUtility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        initializeData()
        invokeUI()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        if(tokenExpireStatus) {
            // showAccessTokenExpireAlert()
            // Show biometrics popup when access token is expired
            showBiometrics()
        }
        // Perform logout action when logout status true
        if (logoutStatus) {
            // Remove access token and refresh token from device storage
            CyberArkPreferenceUtil.remove(Constants.ACCESS_TOKEN)
            CyberArkPreferenceUtil.remove(Constants.ACCESS_TOKEN_IV)
            CyberArkPreferenceUtil.remove(Constants.REFRESH_TOKEN)
            CyberArkPreferenceUtil.remove(Constants.REFRESH_TOKEN_IV)

            // Remove ENROLLMENT_STATUS flag from device storage
            CyberArkPreferenceUtil.remove(PreferenceConstants.ENROLLMENT_STATUS)
            CyberArkPreferenceUtil.apply()

            // Remove OTP enroll data
            CyberArkPreferenceUtil.remove(NotificationConstants.OTP_ENROLL_DATA)

            // Start HomeActivity
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("EXIT", true)
            startActivity(intent)
            finishAffinity()
        }
    }

    /**
     * Initialize notification data
     *
     */
    private fun initializeData() {
        val notificationIntent = intent
        notificationData = notificationIntent.getParcelableExtra(FCMReceiver.NOTIFICATION_DATA)!!

        val otpEnrollData =
            CyberArkPreferenceUtil.getString(NotificationConstants.OTP_ENROLL_DATA, null)
        if(otpEnrollData != null) {
            otpEnrollModel = Gson().fromJson(otpEnrollData, OTPEnrollModel::class.java)
        } else {
            otpEnroll()
        }
        accessTokenData = KeyStoreProvider.get().getAuthToken().toString()
        refreshTokenData = KeyStoreProvider.get().getRefreshToken().toString()

        // Invoke biometric utility instance
        cyberArkBiometricPromptUtility =
            CyberArkBiometricManager().getBiometricUtility(biometricCallback)
        tokenExpireStatus = notificationIntent.getBooleanExtra(NotificationConstants.TOKEN_EXPIRE_STATUS, false)
    }

    /**
     * Invoke notification activity UI elements
     *
     */
    private fun invokeUI() {
        progressBar = findViewById(R.id.progressBar_notification_activity)
        notificationDesc = findViewById(R.id.notification_desc)
        approveButton = findViewById(R.id.button_login)
        denyButton = findViewById(R.id.button_cancel)

        approveButton.setOnClickListener {
            userAccepted = true
            approveNotification()
        }
        denyButton.setOnClickListener {
            userAccepted = false
            denyNotification()
        }
    }

    /**
     * Update notification title and description
     *
     */
    private fun updateUI() {
        title = getString(R.string.acme)
        notificationDesc.text = notificationData.Message
    }

    /**
     * Call API to submit OTP code, notification challenge answer and user accepted status as true
     *
     */
    private fun approveNotification() {
        Log.i("NotificationActivity", "Approve")
        verifyWithAccessToken(true)
    }

    /**
     * Call API to submit OTP code, notification challenge answer and user accepted status as false
     *
     */
    private fun denyNotification() {
        Log.i("NotificationActivity", "Deny")
        verifyWithAccessToken(false)
    }

    /**
     * Verify if the existing access token is valid or not
     * If valid, then call submit OTP API
     * In not valid, then show access token expire alert popup
     *
     * @param userAcceptedStatus : user accepted status, true/false
     */
    private fun verifyWithAccessToken(userAcceptedStatus: Boolean) {
        if (::accessTokenData.isInitialized) {
            var status = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                status = JWTUtils.isAccessTokenExpired(accessTokenData)
            } else {
                Log.i(TAG, "Not supported VERSION.SDK_INT < O")
            }

            if (!status) {
//                // Show access token expire alert popup
//                showAccessTokenExpireAlert()
                // Show biometrics popup when access token is expired
                showBiometrics()
            } else {
                // Show progress indicator
                progressBar.visibility = View.VISIBLE
                val notificationPayload: JSONObject =
                    getNotificationPayload(notificationData.ChallengeAnswer, userAcceptedStatus)
                submitOTP(this, otpEnrollModel, notificationPayload)
            }
        } else {
            Log.i(TAG, "Access Token is not initialized")
        }
    }

    /**
     * Submit OTP code to server
     *
     * @param context: application context
     * @param otpEnrollModel: OTPEnrollModel instance
     * @param notificationPayload: notification payload
     */
    private fun submitOTP(
        context: Context,
        otpEnrollModel: OTPEnrollModel,
        notificationPayload: JSONObject
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            val accessTokenData = KeyStoreProvider.get().getAuthToken()
            if (accessTokenData != null) {
                val submitOTPModel: SubmitOTPModel? = CyberArkAuthProvider.submitOTP(setupFCMUrl(context))
                    .start(context, accessTokenData, otpEnrollModel, notificationPayload)
                handleSubmitOTPResponse(submitOTPModel)
            } else {
                Log.i(TAG, "Access Token is not initialized")
            }
            // Hide progress indicator
            progressBar.visibility = View.GONE
            finish()
        }
    }

    /**
     * Handle submit OTP response
     *
     * @param submitOTPModel: SubmitOTPModel instance
     */
    private fun handleSubmitOTPResponse(submitOTPModel: SubmitOTPModel?) {
        if (submitOTPModel == null) {
            Log.i(TAG, "Submit OTP: Unable to get response from server")
        } else if (!submitOTPModel.success) {
            Log.i(TAG, submitOTPModel.Message)
        } else {
            Log.i(TAG, "User accepted the push notification")
        }
    }

    /**
     * Setup System URL and host URL in CyberArkAccountBuilder
     *
     * @return CyberArkAccountBuilder instance
     */
    private fun setupFCMUrl(context: Context): CyberArkAccountBuilder {
        val account =  AppConfig.setupAccountFromSharedPreference(context)
        return CyberArkAccountBuilder.Builder()
            .systemURL(account.getBaseSystemUrl)
            .hostURL(account.getBaseUrl)
            .build()
    }

    /**
     * Get notification payload object
     *
     * @param challengeAnswer: notification challenge answer
     * @param userAccepted: user accepted status, true/false
     * @return JSONObject
     */
    private fun getNotificationPayload(challengeAnswer: String, userAccepted: Boolean): JSONObject {
        val notificationPayload = JSONObject()
        notificationPayload.put(NotificationConstants.OTP_CHALLENGE_ANSWER, challengeAnswer)
        notificationPayload.put(NotificationConstants.USER_ACCEPTED, userAccepted)
        return notificationPayload
    }

    // *********** Handle access and refresh token expire scenarios Start *********** //
    /**
     * Show alert popup when access token is expired
     */
    private fun showAccessTokenExpireAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.NEGATIVE) {
                    // User cancels dialog
                } else if (buttonType == AlertButtonType.POSITIVE) {
                    // Show biometrics popup when access token is expired
                    showBiometrics()
                }
            }
        })
        enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.dialog_header_text),
            this.getString(R.string.dialog_access_token_expire_desc), false,
            mutableListOf(
                AlertButton("Cancel", AlertButtonType.NEGATIVE),
                AlertButton("OK", AlertButtonType.POSITIVE)
            )
        )
    }

    /**
     * Show alert popup when refresh token is expired
     */
    private fun showRefreshTokenExpireAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.NEGATIVE) {
                    // User cancels dialog
                } else if (buttonType == AlertButtonType.POSITIVE) {
                    // End session if refresh token is expired
                    val account =  AppConfig.setupAccountFromSharedPreference(this@NotificationActivity)
                    logout(account)
                }
            }
        })
        enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.dialog_header_text),
            this.getString(R.string.dialog_refresh_token_expire_desc), false,
            mutableListOf(
                AlertButton("Cancel", AlertButtonType.NEGATIVE),
                AlertButton("OK", AlertButtonType.POSITIVE)
            )
        )
    }
    // *********** Handle access and refresh token expire scenarios End *********** //


    // ****************** Handle refresh token flow Start *********************** //
    /**
     * Get the access token using refresh token
     * and handle API response using active observer
     *
     * @param cyberArkAccountBuilder: CyberArkAccountBuilder instance
     */
    private fun refreshToken(cyberArkAccountBuilder: CyberArkAccountBuilder) {

        val refreshTokenResponseHandler: LiveData<ResponseHandler<RefreshTokenModel>> =
            CyberArkAuthProvider.refreshToken(cyberArkAccountBuilder).start(this, refreshTokenData)

        if (!refreshTokenResponseHandler.hasActiveObservers()) {
            refreshTokenResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        // Save access token in local variable
                        accessTokenData = it.data!!.access_token
                        // Save access token in shared preference using keystore encryption
                        KeyStoreProvider.get().saveAuthToken(accessTokenData)
                        // Show success message using Toast
                        Toast.makeText(
                            this,
                            getString(R.string.access_token_received),
                            Toast.LENGTH_SHORT
                        ).show()
                        val notificationPayload: JSONObject =
                            getNotificationPayload(notificationData.ChallengeAnswer, userAccepted)
                        submitOTP(this, otpEnrollModel, notificationPayload)
                    }
                    ResponseStatus.ERROR -> {
                        progressBar.visibility = View.GONE
                        // Show error message using Toast
                        Toast.makeText(
                            this,
                            "Error: Unable to fetch access token using refresh token",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Show dialog when refresh token is expired
                        showRefreshTokenExpireAlert()
                    }
                    ResponseStatus.LOADING -> {
                        // Hide progress indicator
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }
    // ****************** Handle refresh token flow End *********************** //

    // ******************** Handle logout flow Start ************************* //
    /**
     * End session from custom chrome tab browser
     *
     * @param cyberArkAccountBuilder: CyberArkAccountBuilder instance
     */
    private fun logout(cyberArkAccountBuilder: CyberArkAccountBuilder) {
        logoutStatus = true
        CyberArkAuthProvider.endSession(cyberArkAccountBuilder).start(this)
    }
    // ********************* Handle logout flow End ************************* //

    /**
     * Call API to get OTP key, secret and save into shared preference
     *
     */
    private fun otpEnroll() {
        lifecycleScope.launch(Dispatchers.Main) {
            val accessTokenData = KeyStoreProvider.get().getAuthToken()
            if (accessTokenData != null) {
                val otpEnrollModel: OTPEnrollModel? = CyberArkAuthProvider.otpEnroll(setupFCMUrl(this@NotificationActivity))
                    .start(this@NotificationActivity, accessTokenData)
                // Save OTP enroll data
                val otpEnrollModelString = Gson().toJson(otpEnrollModel)
                CyberArkPreferenceUtil.putString(
                    NotificationConstants.OTP_ENROLL_DATA,
                    otpEnrollModelString
                )
            } else {
                Log.i(TAG, "Access Token is not initialized")
            }
        }
    }

    // ************************ Handle biometrics Start **************************** //
    /**
     * Show all strong biometrics in a prompt
     * negativeButtonText: "Use App Pin" text in order to handle fallback scenario
     * useDevicePin: true/false (true when biometrics is integrated with device pin as fallback else false)
     *
     */
    private fun showBiometrics() {
        cyberArkBiometricPromptUtility.showBioAuthentication(this, null, "Use App Pin", false)
    }

    /**
     * Callback to handle biometrics response
     */
    private val biometricCallback = object : CyberArkBiometricCallback {
        override fun isAuthenticationSuccess(success: Boolean) {
            // Show Authentication success message using Toast
            Toast.makeText(
                this@NotificationActivity,
                getString(R.string.authentication_is_successful),
                Toast.LENGTH_LONG
            ).show()

            // Verify if access token is expired or not
            var status = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                status = JWTUtils.isAccessTokenExpired(accessTokenData)
            } else {
                Log.i(TAG, "Not supported VERSION.SDK_INT < O")
            }
            if (!status) {
                // Invoke API to get access token using refresh token
                val account =  AppConfig.setupAccountFromSharedPreference(this@NotificationActivity)
                refreshToken(account)
            }
        }

        override fun passwordAuthenticationSelected() {
            Toast.makeText(
                this@NotificationActivity,
                "Password authentication is selected",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun showErrorMessage(message: String) {
            Toast.makeText(this@NotificationActivity, message, Toast.LENGTH_LONG).show()
        }

        override fun isHardwareSupported(boolean: Boolean) {
            if (!boolean) {
                Toast.makeText(
                    this@NotificationActivity,
                    "Hardware is not supported",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun isSdkVersionSupported(boolean: Boolean) {
            Toast.makeText(
                this@NotificationActivity,
                "SDK version is not supported",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun isBiometricEnrolled(boolean: Boolean) {
            if (!boolean) {
                Toast.makeText(
                    this@NotificationActivity,
                    "Biometric is not enrolled",
                    Toast.LENGTH_LONG
                ).show()
                // Show biometric enrollment alert popup
                showBiometricsEnrollmentAlert()
            }
        }

        override fun biometricErrorSecurityUpdateRequired() {
            Toast.makeText(
                this@NotificationActivity,
                "Biometric error, security update is required",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Show biometrics enrollment popup if not registered
     *
     */
    private fun showBiometricsEnrollmentAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.NEGATIVE) {
                    // User cancels dialog
                } else if (buttonType == AlertButtonType.POSITIVE) {
                    // Launch device settings screen for biometrics setup
                    launchBiometricSetup()
                }
            }
        })
        enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.dialog_header_text),
            this.getString(R.string.dialog_biometric_desc), false,
            mutableListOf(
                AlertButton("Cancel", AlertButtonType.NEGATIVE),
                AlertButton("OK", AlertButtonType.POSITIVE)
            )
        )
    }

    /**
     * Invoke security settings screen to register biometrics
     *
     */
    private fun launchBiometricSetup() {
        this.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
    // ************************ Handle biometrics End ******************************** //
}