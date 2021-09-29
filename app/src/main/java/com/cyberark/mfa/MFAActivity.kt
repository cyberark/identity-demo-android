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

package com.cyberark.mfa

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.cyberark.identity.activity.CyberArkQRCodeLoginActivity
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.*
import com.cyberark.identity.util.biometric.CyberArkBiometricCallback
import com.cyberark.identity.util.biometric.CyberArkBiometricManager
import com.cyberark.identity.util.biometric.CyberArkBiometricPromptUtility
import com.cyberark.identity.util.jwt.JWTUtils
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.utils.PreferenceConstants
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
class MFAActivity : AppCompatActivity() {

    private val tag: String? = MFAActivity::class.simpleName

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    // Refresh token data variable
    private lateinit var refreshTokenData: String

    // Access token data variable
    private lateinit var accessTokenData: String

    // Enroll, QR Authenticator and logout button variables
    private lateinit var enrollButton: Button
    private lateinit var logOutButton: Button

    // Device biometrics checkbox variables
    private lateinit var biometricsOnAppLaunchCheckbox: CheckBox
    private lateinit var biometricsOnRefreshTokenCheckbox: CheckBox

    // SDK biometrics utility class variable
    private lateinit var cyberArkBiometricPromptUtility: CyberArkBiometricPromptUtility

    // Flags in order to handle UI and flow
    private var biometricsOnAppLaunchRequested: Boolean = false
    private var isBiometricsAuthenticated: Boolean = false
    private var logoutStatus: Boolean = false
    private var isEnrolled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mfa)

        // Invoke UI elements
        invokeUI()

        // Initialize all data
        initializeData()

        // Setup account
        val account = setupAccount()

        // Update UI components
        updateUI(account)

        // Perform enroll device
        enrollButton.setOnClickListener {
            handleClick(it, account)
        }

        // Perform logout
        logOutButton.setOnClickListener {
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
        // Update biometrics status and handle logout scenario
        biometricsOnRefreshTokenCheckbox.isEnabled = true
        biometricsOnAppLaunchCheckbox.isEnabled = true

        // Perform logout action when logout status true
        if (logoutStatus) {
            // Remove access token and refresh token from device storage
            CyberArkPreferenceUtil.remove(Constants.ACCESS_TOKEN)
            CyberArkPreferenceUtil.remove(Constants.ACCESS_TOKEN_IV)
            CyberArkPreferenceUtil.remove(Constants.REFRESH_TOKEN)
            CyberArkPreferenceUtil.remove(Constants.REFRESH_TOKEN_IV)

            // Remove ENROLLMENT_STATUS flag from device storage
            CyberArkPreferenceUtil.remove(PreferenceConstants.ENROLLMENT_STATUS)
            CyberArkPreferenceUtil.clear()

            // Start HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
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
        logOutButton = findViewById(R.id.button_logout)
        enrollButton = findViewById(R.id.button_enroll)
        biometricsOnAppLaunchCheckbox = findViewById(R.id.biometrics_on_app_launch_checkbox)
        biometricsOnRefreshTokenCheckbox = findViewById(R.id.biometrics_on_refresh_token_checkbox)

        // Invoke biometric utility instance
        cyberArkBiometricPromptUtility = CyberArkBiometricManager().getBiometricUtility(biometricCallback)
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
     * Set-up account for OAuth 2.0 PKCE driven flow
     * update account configuration in "res/values/config.xml"
     *
     * @return cyberArkAccountBuilder: CyberArkAccountBuilder instance
     */
    private fun setupAccount(): CyberArkAccountBuilder {
        val cyberArkAccountBuilder = CyberArkAccountBuilder.Builder()
            .systemURL(getString(R.string.cyberark_account_system_url))
            .hostURL(getString(R.string.cyberark_account_host_url))
            .clientId(getString(R.string.cyberark_account_client_id))
            .appId(getString(R.string.cyberark_account_app_id))
            .responseType(getString(R.string.cyberark_account_response_type))
            .scope(getString(R.string.cyberark_account_scope))
            .redirectUri(getString(R.string.cyberark_account_redirect_uri))
            .build()
        return cyberArkAccountBuilder
    }

    /**
     * Verify enrollment status and update UI accordingly
     * Get the shared preference status and handle device biometrics on app launch
     * Get the shared preference status and handle device biometrics when access token expires
     *
     */
    private fun updateUI(cyberArkAccountBuilder: CyberArkAccountBuilder) {
        // Verify enrollment status and update button text
        if (::accessTokenData.isInitialized) {
            logOutButton.isEnabled = true
            if (CyberArkPreferenceUtil.getBoolean(PreferenceConstants.ENROLLMENT_STATUS, false)) {
                isEnrolled = true
                enrollButton.setText(R.string.tv_qr_authenticator)
            }
        }
        // Get the shared preference status and handle device biometrics on app launch
        biometricsOnAppLaunchRequested =
            CyberArkPreferenceUtil.getBoolean(getString(R.string.pref_key_invoke_biometrics_on_app_launch), false)
        biometricsOnAppLaunchCheckbox.isChecked = biometricsOnAppLaunchRequested
        biometricsOnAppLaunchCheckbox.setOnClickListener {
            handleClick(it, cyberArkAccountBuilder)
        }
        // Get the shared preference status and handle device biometrics when access token expires
        biometricsOnRefreshTokenCheckbox.isChecked =
            CyberArkPreferenceUtil.getBoolean(getString(R.string.pref_key_invoke_biometrics_when_access_token_expires), false)
        biometricsOnRefreshTokenCheckbox.setOnClickListener {
            handleClick(it, cyberArkAccountBuilder)
        }
        // Get the shared preference status and update the biometrics selection
        if (!CyberArkPreferenceUtil.contains(getString(R.string.pref_key_invoke_biometrics_on_app_launch))) {
            saveBiometricsRequestOnAppLaunch(true)
            saveBiometricsRequestOnRefreshToken(true)
        }
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    status = JWTUtils.isAccessTokenExpired(accessTokenData)
                } else {
                    Log.i(tag, "Not supported VERSION.SDK_INT < O")
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
                        startQRCodeAuthenticator()
                    }
                }
            } else {
                Log.i(tag, "Access Token is not initialized")
            }
        } else if (view.id == R.id.button_logout) {
            // Start end session
            logout(cyberArkAccountBuilder)
        } else if (view.id == R.id.biometrics_on_refresh_token_checkbox) {
            // Save biometrics request status (when access token expires) in shared preference
            saveBiometricsRequestOnRefreshToken(biometricsOnRefreshTokenCheckbox.isChecked)
        } else if (view.id == R.id.biometrics_on_app_launch_checkbox) {
            // Save biometrics request status (on app launch) in shared preference
            saveBiometricsRequestOnAppLaunch(biometricsOnAppLaunchCheckbox.isChecked)
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
                        // Show enrollment success message using Toast
                        Toast.makeText(
                            this,
                            "Enrolled successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Save enrollment status
                        CyberArkPreferenceUtil.putBoolean(PreferenceConstants.ENROLLMENT_STATUS, true)
                        // Update UI status
                        logOutButton.isEnabled = true
                        isEnrolled = true
                        enrollButton.setText(R.string.tv_qr_authenticator)
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
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
                            "Received New Access Token" + ResponseStatus.SUCCESS.toString(),
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
                            "Error: Unable to fetch access token using refresh token" + ResponseStatus.ERROR.toString(),
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
                    val account = setupAccount()
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
     * Save "Invoke biometrics on app launch" status in shared preference
     *
     * @param checked: Boolean
     */
    private fun saveBiometricsRequestOnAppLaunch(checked: Boolean) {
        var value = checked
        if (!CyberArkPreferenceUtil.contains(getString(R.string.pref_key_invoke_biometrics_on_app_launch))) {
            value = true
            biometricsOnRefreshTokenCheckbox.isChecked = value
        }
        CyberArkPreferenceUtil.putBoolean(getString(R.string.pref_key_invoke_biometrics_on_app_launch), value)
    }

    /**
     * Save "Invoke biometrics when access token expires" status in shared preference
     *
     * @param checked: Boolean
     */
    private fun saveBiometricsRequestOnRefreshToken(checked: Boolean) {
        var value = checked
        if (!CyberArkPreferenceUtil.contains(getString(R.string.pref_key_invoke_biometrics_when_access_token_expires))) {
            value = true
            biometricsOnAppLaunchCheckbox.isChecked = value
        }
        CyberArkPreferenceUtil.putBoolean(getString(R.string.pref_key_invoke_biometrics_when_access_token_expires), value)
    }

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
                this,
                "Authentication success",
                Toast.LENGTH_LONG
            ).show()

            // Update status
            isBiometricsAuthenticated = true
            biometricsOnRefreshTokenCheckbox.isEnabled = true
            biometricsOnAppLaunchCheckbox.isEnabled = true

            // Verify if access token is expired or not
            var status = false
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                status = JWTUtils.isAccessTokenExpired(accessTokenData)
            } else {
                Log.i(tag, "Not supported VERSION.SDK_INT < O")
            }
            if (!status) {
                // Invoke API to get access token using refresh token
                val account = setupAccount()
                refreshToken(account)
            }
        }

        override fun passwordAuthenticationSelected() {
            Toast.makeText(
                this@MFAActivity,
                "Password authentication selected",
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
                    "Hardware not supported",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun isSdkVersionSupported(boolean: Boolean) {
            Toast.makeText(
                this@MFAActivity,
                "SDK version not supported",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun isBiometricEnrolled(boolean: Boolean) {
            if (!boolean) {
                // Show biometric enrollment alert popup
                showBiometricsEnrollmentAlert()
            }
        }

        override fun biometricErrorSecurityUpdateRequired() {
            Toast.makeText(
                this@MFAActivity,
                "Biometric security updates required",
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