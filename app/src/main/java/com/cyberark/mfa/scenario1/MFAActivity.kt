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

package com.cyberark.mfa.scenario1

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.cyberark.identity.activity.CyberArkQRCodeLoginActivity
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.OTPEnrollModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.data.model.SendFCMTokenModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.*
import com.cyberark.identity.util.biometric.CyberArkBiometricCallback
import com.cyberark.identity.util.biometric.CyberArkBiometricManager
import com.cyberark.identity.util.biometric.CyberArkBiometricPromptUtility
import com.cyberark.identity.util.jwt.JWTUtils
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.notification.NotificationConstants
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.activity.SettingsActivity
import com.cyberark.mfa.activity.WelcomeActivity
import com.cyberark.mfa.fcm.FCMTokenInterface
import com.cyberark.mfa.fcm.FCMTokenUtil
import com.cyberark.mfa.utils.AppConfig
import com.cyberark.mfa.utils.PreferenceConstants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Implementing SDK features in MFAActivity
 * 1. Enroll
 * 2. QR Code Authenticator
 * 3. logout
 * 4. Invoke biometrics on app launch
 * 5. Invoke biometrics when access token expires
 *
 */
class MFAActivity : AppCompatActivity(), FCMTokenInterface {

    companion object {
        private val TAG = MFAActivity::class.simpleName
    }

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    // Refresh token data variable
    private lateinit var refreshTokenData: String

    // Access token data variable
    private lateinit var accessTokenData: String

    // Enroll, QR Authenticator and logout button variables
    private lateinit var enrollButton: Button
    private lateinit var headerText: TextView
    private lateinit var contentText: TextView

    // SDK biometrics utility class variable
    private lateinit var cyberArkBiometricPromptUtility: CyberArkBiometricPromptUtility

    // Flags in order to handle UI and flow
    private var biometricsOnAppLaunchRequested: Boolean = false
    private var isBiometricsAuthenticated: Boolean = false
    private var logoutStatus: Boolean = false
    private var isEnrolled: Boolean = false
    private var biometricsOnQRCodeLaunchRequested: Boolean = false
    private var biometricsOnQRCodeStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mfa)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        title = getString(R.string.acme)

        // Invoke UI elements
        invokeUI()

        // Initialize all data
        initializeData()

        // Update UI components
        updateUI()

        // Setup account
        val account =  AppConfig.setupAccountFromSharedPreference(this)

        // Perform enroll device
        enrollButton.setOnClickListener {
            handleClick(it, account)
        }
    }

    override fun onResume() {
        super.onResume()
        if (biometricsOnAppLaunchRequested && !isBiometricsAuthenticated) {
            // Invoke biometrics prompt
            showBiometrics()
        } else {
            cleanUp()
        }
    }

    private fun cleanUp() {
        // Perform logout action when logout status true
        if (logoutStatus) {
            // Remove access token and refresh token from device storage
            CyberArkPreferenceUtil.remove(Constants.ACCESS_TOKEN)
            CyberArkPreferenceUtil.remove(Constants.ACCESS_TOKEN_IV)
            CyberArkPreferenceUtil.remove(Constants.REFRESH_TOKEN)
            CyberArkPreferenceUtil.remove(Constants.REFRESH_TOKEN_IV)

            // Remove ENROLLMENT_STATUS flag from device storage
            CyberArkPreferenceUtil.remove(PreferenceConstants.ENROLLMENT_STATUS)

            // Remove biometrics settings status
            CyberArkPreferenceUtil.remove(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH)
            CyberArkPreferenceUtil.remove(PreferenceConstants.INVOKE_BIOMETRICS_ON_QR_CODE_LAUNCH)
            CyberArkPreferenceUtil.remove(PreferenceConstants.INVOKE_BIOMETRICS_ON_TOKEN_EXPIRES)

            CyberArkPreferenceUtil.apply()

            // Start HomeActivity
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Invoke all UI elements and initialize into variables
     *
     */
    private fun invokeUI() {
        progressBar = findViewById(R.id.progressBar_mfa_activity)
        enrollButton = findViewById(R.id.button_enroll)

        headerText = findViewById(R.id.header_text)
        headerText.text = getString(R.string.click_on_active_mfa)
        contentText = findViewById(R.id.content_text)
        contentText.text = getString(R.string.enrolling_the_mobile_device_enables)

        // Invoke biometric utility instance
        cyberArkBiometricPromptUtility =
            CyberArkBiometricManager().getBiometricUtility(biometricCallback)

        setupHyperlink()
    }

    private fun setupHyperlink() {
        val linkTextView: TextView = findViewById(R.id.end_text)
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * Get access token and refresh token from device storage using keystore
     * and initialize into variables
     *
     */
    private fun initializeData() {
        accessTokenData = KeyStoreProvider.get().getAuthToken().toString()
        refreshTokenData = KeyStoreProvider.get().getRefreshToken().toString()
    }

    /**
     * Verify enrollment status and update UI accordingly
     * Get the shared preference status and handle device biometrics on app launch
     * Get the shared preference status and handle device biometrics when access token expires
     *
     */
    private fun updateUI() {
        // Verify enrollment status and update button text
        if (::accessTokenData.isInitialized) {
            if (CyberArkPreferenceUtil.getBoolean(PreferenceConstants.ENROLLMENT_STATUS, false)) {
                isEnrolled = true
                enrollButton.setText(R.string.tv_qr_authenticator)
                headerText.text = getString(R.string.click_on_qr_code_authenticator)
                contentText.text = ""
                contentText.visibility = View.GONE
            }
        }
        // Get the shared preference status and update the biometrics selection
        if (!CyberArkPreferenceUtil.contains(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH)) {
            CyberArkPreferenceUtil.putBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH,
                true
            )

            CyberArkPreferenceUtil.putBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_QR_CODE_LAUNCH,
                true
            )

            CyberArkPreferenceUtil.putBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_TOKEN_EXPIRES,
                true
            )
        }

        // Get the shared preference status and handle device biometrics on app launch
        biometricsOnAppLaunchRequested =
            CyberArkPreferenceUtil.getBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH,
                false
            )

        // Get the shared preference status and handle device biometrics on QR Code launch
        biometricsOnQRCodeStatus =
            CyberArkPreferenceUtil.getBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_QR_CODE_LAUNCH,
                false
            )
    }


    // ******************** Handle all click actions Start ************************* //
    /**
     * Handle all click actions
     *
     * @param view
     */
    private fun handleClick(view: View, cyberArkAccountBuilder: CyberArkAccountBuilder) {
        if (!isBiometricsAuthenticated && biometricsOnAppLaunchRequested) {
            // Show biometrics prompt
            showBiometrics()
            return
        }
        if (view.id == R.id.button_enroll) {
            if (::accessTokenData.isInitialized) {
                // Get the access token expire status
                var status = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    status = JWTUtils.isAccessTokenExpired(accessTokenData)
                } else {
                    Log.i(TAG, "Not supported VERSION.SDK_INT < O")
                }

                if (!isEnrolled) {
                    // Handle enrollment flow based on access token expire status
                    if (!status) {
                        // Show access token expire alert popup
                        showAccessTokenExpireAlert()
                    } else {
                        // Start enrollment flow
                        enroll(cyberArkAccountBuilder)
                    }
                } else {
                    // Handle QR Code Authenticator flow based on access token expire status
                    if (!status) {
                        // Show access token expire alert popup
                        showAccessTokenExpireAlert()
                    } else {
                        // Start QR Code Authenticator flow
                        if(biometricsOnQRCodeStatus) {
                            biometricsOnQRCodeLaunchRequested = true
                            showBiometrics()
                        } else {
                            startQRCodeAuthenticator()
                        }
                    }
                }
            } else {
                Log.i(TAG, "Access Token is not initialized")
            }
        }
    }
    // ******************** Handle all click actions End ************************* //


    // ******************** Handle enrollment flow Start ************************* //
    /**
     * Enroll device using access token
     * and handle API response using active observer
     *
     */
    private fun enroll(cyberArkAccountBuilder: CyberArkAccountBuilder) {
        val authResponseHandler: LiveData<ResponseHandler<EnrollmentModel>> =
            CyberArkAuthProvider.enroll(cyberArkAccountBuilder).start(this, accessTokenData)

        // Verify if there is any active observer, if not then add observer to get API response
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        // Obtain FCM token and upload to server
                        obtainFCMToken()
                        // Show enrollment success message using Toast
                        Toast.makeText(
                            this,
                            "Enrolled successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Save enrollment status
                        CyberArkPreferenceUtil.putBoolean(
                            PreferenceConstants.ENROLLMENT_STATUS,
                            true
                        )
                        // Update UI status
                        isEnrolled = true
                        enrollButton.setText(R.string.tv_qr_authenticator)
                        headerText.text = getString(R.string.click_on_qr_code_authenticator)
                        contentText.text = ""
                        contentText.visibility = View.GONE
                    }
                    ResponseStatus.ERROR -> {
                        // Show enrollment error message using Toast
                        Toast.makeText(
                            this,
                            "Error: Unable to Enroll device",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                    }
                    ResponseStatus.LOADING -> {
                        // Show progress indicator
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }
    // ******************** Handle enrollment flow End ************************* //


    // ****************** Handle QR Code Authenticator flow Start *********************** //
    /**
     * Start QR Code authenticator Activity
     *
     */
    private fun startQRCodeAuthenticator() {
        val intent = Intent(this, CyberArkQRCodeLoginActivity::class.java)
        intent.putExtra("access_token", accessTokenData)
        startForResult.launch(intent)
    }

    /**
     * Callback to handle QR Code Authenticator result
     */
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Use key "QR_CODE_AUTH_RESULT" to receive result data
                val data = result.data?.getStringExtra("QR_CODE_AUTH_RESULT")
                // Show QR Code Authenticator result using Toast
                Toast.makeText(
                    this,
                    data.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    // ****************** Handle QR Code Authenticator flow End *********************** //


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
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
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
                    val account =  AppConfig.setupAccountFromSharedPreference(this@MFAActivity)
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
                this@MFAActivity,
                getString(R.string.authentication_is_successful),
                Toast.LENGTH_LONG
            ).show()

            // Update status
            isBiometricsAuthenticated = true

            // Verify if access token is expired or not
            var status = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                status = JWTUtils.isAccessTokenExpired(accessTokenData)
            } else {
                Log.i(TAG, "Not supported VERSION.SDK_INT < O")
            }
            if (!status) {
                // Invoke API to get access token using refresh token
                val account =  AppConfig.setupAccountFromSharedPreference(this@MFAActivity)
                refreshToken(account)
            } else if(biometricsOnQRCodeLaunchRequested) {
                startQRCodeAuthenticator()
            }
        }

        override fun passwordAuthenticationSelected() {
            Toast.makeText(
                this@MFAActivity,
                "Password authentication is selected",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun showErrorMessage(message: String) {
            Toast.makeText(this@MFAActivity, message, Toast.LENGTH_LONG).show()
        }

        override fun isHardwareSupported(boolean: Boolean) {
            if (!boolean) {
                Toast.makeText(
                    this@MFAActivity,
                    "Hardware is not supported",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun isSdkVersionSupported(boolean: Boolean) {
            Toast.makeText(
                this@MFAActivity,
                "SDK version is not supported",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun isBiometricEnrolled(boolean: Boolean) {
            if (!boolean) {
                Toast.makeText(
                    this@MFAActivity,
                    "Biometric is not enrolled",
                    Toast.LENGTH_LONG
                ).show()
                // Show biometric enrollment alert popup
                showBiometricsEnrollmentAlert()
            }
        }

        override fun biometricErrorSecurityUpdateRequired() {
            Toast.makeText(
                this@MFAActivity,
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


    // ******************* Handle notification Start *************************** //
    /**
     * Obtain FCM token immediately after the successful enrollment
     */
    private fun obtainFCMToken() {
        val fcmTokenUtil = FCMTokenUtil()
        fcmTokenUtil.getFCMToken(this)
    }

    override fun onFcmTokenReceived(fcmToken: String) {
        uploadFCMTokenToCyberArkServer(fcmToken)
    }

    override fun onFcmTokenFailure(exception: Throwable?) {
        // Hide progress indicator
        progressBar.visibility = View.GONE
        Log.e(TAG, "Error obtaining FCM token", exception)
    }

    /**
     * Upload FCM token to CyberArk server
     *
     * @param token: FCM token
     */
    private fun uploadFCMTokenToCyberArkServer(token: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val accessTokenData = KeyStoreProvider.get().getAuthToken()
            if (accessTokenData != null) {
                val sendFCMTokenModel: SendFCMTokenModel? =
                    CyberArkAuthProvider.sendFCMToken(setupFCMUrl())
                        .start(this@MFAActivity, token, accessTokenData)
                handleUploadFCMTokenResponse(sendFCMTokenModel)
            } else {
                Log.i(TAG, "Access Token is not initialized")
            }
        }
    }

    /**
     * Handle upload FCM token response
     *
     * @param sendFCMTokenModel: SendFCMTokenModel instance
     */
    private fun handleUploadFCMTokenResponse(sendFCMTokenModel: SendFCMTokenModel?) {
        // Hide progress indicator
        progressBar.visibility = View.GONE
        if (sendFCMTokenModel == null) {
            Toast.makeText(
                this,
                "Upload FCM Token: Unable to get response from server",
                Toast.LENGTH_SHORT
            ).show()
            Log.i(TAG, "Upload FCM Token: Unable to get response from server")
        } else if (!sendFCMTokenModel.Status) {
            Toast.makeText(
                this,
                "Unable to upload FCM Token to Server",
                Toast.LENGTH_SHORT
            ).show()
            Log.i(TAG, "Unable to upload FCM Token to Server")
        } else {
            Log.i(TAG, "Uploaded FCM Token to Server successfully")
            // Hide progress indicator
            progressBar.visibility = View.VISIBLE
            otpEnroll()
        }
    }

    /**
     * Setup System URL and host URL in CyberArkAccountBuilder
     *
     * @return CyberArkAccountBuilder instance
     */
    private fun setupFCMUrl(): CyberArkAccountBuilder {
        val account =  AppConfig.setupAccountFromSharedPreference(this)
        return CyberArkAccountBuilder.Builder()
            .systemURL(account.getBaseSystemUrl)
            .hostURL(account.getBaseUrl)
            .build()
    }

    /**
     * Call API to get OTP key, secret and save into shared preference
     *
     */
    private fun otpEnroll() {
        lifecycleScope.launch(Dispatchers.Main) {
            val accessTokenData = KeyStoreProvider.get().getAuthToken()
            if (accessTokenData != null) {
                val otpEnrollModel: OTPEnrollModel? = CyberArkAuthProvider.otpEnroll(setupFCMUrl())
                    .start(this@MFAActivity, accessTokenData)
                // Save OTP enroll data
                val otpEnrollModelString = Gson().toJson(otpEnrollModel)
                CyberArkPreferenceUtil.putString(
                    NotificationConstants.OTP_ENROLL_DATA,
                    otpEnrollModelString
                )
                Toast.makeText(
                    this@MFAActivity,
                    "OTP Enroll completed",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.i(TAG, "Access Token is not initialized")
            }
            // Hide progress indicator
            progressBar.visibility = View.GONE
        }
    }
    // ******************* Handle notification End *************************** //


    // **************** Handle menu settings click action Start *********************** //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.logout_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            //Start Settings activity
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("from_activity", "MFAActivity")
            startActivity(intent)
            true
        }
        R.id.action_logout -> {
            val account =  AppConfig.setupAccountFromSharedPreference(this)
            // Start end session
            logout(account)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    // **************** Handle menu settings click action End *********************** //
}