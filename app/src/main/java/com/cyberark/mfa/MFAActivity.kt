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
import com.cyberark.identity.CyberarkQRCodeLoginActivity
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.data.model.EnrollmentModel
import com.cyberark.identity.data.model.RefreshTokenModel
import com.cyberark.identity.provider.CyberarkAuthProvider
import com.cyberark.identity.util.*
import com.cyberark.identity.util.biometric.BiometricAuthenticationCallback
import com.cyberark.identity.util.biometric.BiometricManager
import com.cyberark.identity.util.biometric.BiometricPromptUtility
import com.cyberark.identity.util.jwt.JWTUtils
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberarkPreferenceUtils
import java.util.*

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
    private lateinit var logOut: Button

    // Device biometrics checkbox variables
    private lateinit var launchWithBio: CheckBox
    private lateinit var biometricReqOnRefresh: CheckBox

    // SDK biometrics utility class variable
    private lateinit var bioMetric: BiometricPromptUtility

    // Status update flag in order to handle UI and flow
    private var isAuthenticationReq: Boolean = false
    private var logoutStatus: Boolean = false
    private var isAuthenticated: Boolean = false
    private var isEnrolled:Boolean = false

    /**
     * Callback to handle QR Authenticator result
     */
    private val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data?.getStringExtra("QR_CODE_AUTH_RESULT")
                    Log.i(tag, "data :: " + data.toString())
                    Toast.makeText(
                            this,
                            data.toString(),
                            Toast.LENGTH_LONG
                    ).show()
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mfa)

        // Invoke UI elements
        invokeUI()

        // Perform initialize data
        initializeData()

        // Perform update UI
        updateUI()

        // Perform enroll device
        enrollButton.setOnClickListener {
            handleClick(it)
        }

        // Perform logout
        logOut.setOnClickListener {
            handleClick(it)
        }

        // Invoke biometric utility instance
        bioMetric = BiometricManager().getBiometricUtility(biometricCallback)
    }

    override fun onResume() {
        super.onResume()
        if (isAuthenticationReq && !isAuthenticated) {
            // Invoke biometrics prompt
            showBiometric()
        } else {
            // Update flag status and handle logout scenario
            biometricReqOnRefresh.isEnabled = true
            launchWithBio.isEnabled = true
            if (logoutStatus) {
                // Clear storage on logout
                CyberarkPreferenceUtils.remove(Constants.AUTH_TOKEN)
                CyberarkPreferenceUtils.remove(Constants.AUTH_TOKEN_IV)
                CyberarkPreferenceUtils.remove(Constants.REFRESH_TOKEN)
                CyberarkPreferenceUtils.remove(Constants.REFRESH_TOKEN_IV)
                CyberarkPreferenceUtils.remove("ENROLLMENT_STATUS")
                CyberarkPreferenceUtils.clear()

                // Start HomeActivity in logout action success
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * Invoke all UI elements
     */
    private fun invokeUI() {
        progressBar = findViewById(R.id.progressBar_mfa_activity)
        logOut = findViewById(R.id.button_end_session)
        enrollButton = findViewById(R.id.button_enroll)
        launchWithBio = findViewById(R.id.biometricReq)
        biometricReqOnRefresh = findViewById(R.id.biometricReqOnRefresh)
    }

    /**
     * Get access token and refresh token from device storage using keystore and initialize in variables
     */
    private fun initializeData() {
        accessTokenData = KeyStoreProvider.get().getAuthToken().toString()
        refreshTokenData = KeyStoreProvider.get().getRefreshToken().toString()
        val status = JWTUtils.isAccessTokenExpired(accessTokenData)
        Log.i(tag, "Access Token Status $status")
    }

    /**
     * Verify and access token status and enrollment status and update UI accordingly
     */
    private fun updateUI() {
        if (::accessTokenData.isInitialized) {
            logOut.isEnabled = true
            if (CyberarkPreferenceUtils.getBoolean("ENROLLMENT_STATUS", false)) {
                isEnrolled = true
                enrollButton.setText(R.string.tv_qr_authenticator)
            }
        }
        // Handle device biometrics
        isAuthenticationReq = CyberarkPreferenceUtils.getBoolean(getString(R.string.pref_key_is_biometric_req), false)
        launchWithBio.isChecked = isAuthenticationReq
        launchWithBio.setOnClickListener {
            handleClick(it)
        }

        biometricReqOnRefresh.isChecked = CyberarkPreferenceUtils.getBoolean(getString(R.string.pref_key_refresh_bio_req), false)
        biometricReqOnRefresh.setOnClickListener {
            handleClick(it)
        }
        if (!CyberarkPreferenceUtils.contains(getString(R.string.pref_key_is_biometric_req))) {
            saveBioReqOnAppLaunch(true)
            saveRefreshBio(true)
        }
    }

    /**
     * Enroll device and receive access token and refresh token
     */
    private fun enroll() {
        val authResponseHandler: LiveData<ResponseHandler<EnrollmentModel>> =
                CyberarkAuthProvider.enroll().start(this, accessTokenData)
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {

                        //TODO.. need to verify and remove all logs
                        Log.i(tag, ResponseStatus.SUCCESS.toString())
                        Log.i(tag, it.data.toString())
                        Log.i(tag, it.data!!.success.toString())
                        logOut.isEnabled = true

                        Toast.makeText(
                                this,
                                "Enrolled successfully",
                                Toast.LENGTH_SHORT
                        ).show()

                        CyberarkPreferenceUtils.putBoolean("ENROLLMENT_STATUS", true)
                        isEnrolled = true
                        enrollButton.setText(R.string.tv_qr_authenticator)
                        progressBar.visibility = View.GONE
                    }
                    ResponseStatus.ERROR -> {
                        Log.i(tag, ResponseStatus.ERROR.toString())
                        Toast.makeText(
                                this,
                                "Error: Unable to Enroll device",
                                Toast.LENGTH_SHORT
                        ).show()
                        progressBar.visibility = View.GONE
                    }
                    ResponseStatus.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    /**
     * Set-up account for OAuth 2.0 PKCE driven flow
     */
    private fun setupAccount(): CyberarkAccountBuilder {
        val cyberarkAccountBuilder = CyberarkAccountBuilder.Builder()
                .clientId(getString(R.string.cyberark_account_client_id))
                .domainURL(getString(R.string.cyberark_account_host))
                .appId(getString(R.string.cyberark_account_app_id))
                .responseType(getString(R.string.cyberark_account_response_type))
                .scope(getString(R.string.cyberark_account_scope))
                .redirectUri(getString(R.string.cyberark_account_redirect_uri))
                .build()
        Log.i(tag, cyberarkAccountBuilder.OAuthBaseURL)
        return cyberarkAccountBuilder
    }

    /**
     * End session from custom tab browser
     */
    private fun endSession(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        logoutStatus = true
        CyberarkAuthProvider.endSession(cyberarkAccountBuilder).start(this)
    }

    /**
     * Get the access token using refresh token
     */
    private fun getAccessTokenUsingRefreshToken(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        val refreshTokenResponseHandler: LiveData<ResponseHandler<RefreshTokenModel>> =
                CyberarkAuthProvider.refreshToken(cyberarkAccountBuilder).start(this, refreshTokenData)
        if (!refreshTokenResponseHandler.hasActiveObservers()) {
            refreshTokenResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {

                        //TODO.. need to verify and remove all logs
                        Log.i(tag, ResponseStatus.SUCCESS.toString())
                        Log.i(tag, it.data.toString())
                        Log.i(tag, it.data!!.access_token)

                        accessTokenData = it.data!!.access_token

                        //Save access token in keystore
                        KeyStoreProvider.get().saveAuthToken(accessTokenData)

                        Toast.makeText(
                                this,
                                "Received New Access Token",
                                Toast.LENGTH_SHORT
                        ).show()
                        progressBar.visibility = View.GONE
                    }
                    ResponseStatus.ERROR -> {
                        Log.i(tag, ResponseStatus.ERROR.toString())
                        progressBar.visibility = View.GONE

                        Toast.makeText(
                                this,
                                "Error: Unable to fetch access token using refresh token",
                                Toast.LENGTH_SHORT
                        ).show()
                        // Show refresh token expire dialog
                        showRefreshTokenExpireAlert()
                    }
                    ResponseStatus.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    /**
     * Handle all click actions
     */
    private fun handleClick(view: View) {
        if (!isAuthenticated && isAuthenticationReq) {
            showBiometric()
            return
        }
        if (view.id == R.id.button_enroll) {
            if (!isEnrolled) {
                if (::accessTokenData.isInitialized) {
                    val status = JWTUtils.isAccessTokenExpired(accessTokenData)
                    Log.i(tag, "Access Token Status $status")
                    if(!status) {
                        showAccessTokenExpireAlert()
                    } else {
                        enroll()
                    }
                } else {
                    //TODO.. handle error scenario
                    Log.i(tag, "Access Token is not initialized")
                }
            }else {
                if (::accessTokenData.isInitialized) {
                    val status = JWTUtils.isAccessTokenExpired(accessTokenData)
                    Log.i(tag, "Access Token Status " + status)
                    if(!status) {
                        showAccessTokenExpireAlert()
                    } else {
                        val intent = Intent(this, CyberarkQRCodeLoginActivity::class.java)
                        intent.putExtra("access_token", accessTokenData)
                        startForResult.launch(intent)
                    }
                } else {
                    //TODO.. handle error scenario
                    Log.i(tag, "Access Token is not initialized")
                }
            }
        }

        else if (view.id == R.id.button_end_session) {
            val account = setupAccount()
            endSession(account)
        } else if (view.id == R.id.biometricReqOnRefresh) {
            saveRefreshBio(biometricReqOnRefresh.isChecked)
        } else if (view.id == R.id.biometricReq) {
            saveBioReqOnAppLaunch(launchWithBio.isChecked)
        }
    }

    /**
     * Save "Invoke biometrics on app launch" status in shared preference
     */
    private fun saveBioReqOnAppLaunch(checked: Boolean) {
        var value = checked
        if (!CyberarkPreferenceUtils.contains(getString(R.string.pref_key_is_biometric_req))) {
            value = true
            biometricReqOnRefresh.isChecked = value
        }
        CyberarkPreferenceUtils.putBoolean(getString(R.string.pref_key_is_biometric_req), value)
    }

    /**
     * Save "Invoke biometrics when access token expires" status in shared preference
     */
    private fun saveRefreshBio(checked: Boolean) {
        var value = checked
        if (!CyberarkPreferenceUtils.contains(getString(R.string.pref_key_refresh_bio_req))) {
            value = true
            launchWithBio.isChecked = value
        }
        CyberarkPreferenceUtils.putBoolean(getString(R.string.pref_key_refresh_bio_req), value)
    }

    /**
     * Show all strong biometrics in a prompt
     */
    private fun showBiometric() {
        bioMetric.showBioAuthentication(this, null, "Use App Pin", false)
    }

    /**
     * Callback to handle biometrics response
     */
    private val biometricCallback = object : BiometricAuthenticationCallback {
        override fun isAuthenticationSuccess(success: Boolean) {
            Toast.makeText(
                    this@MFAActivity,
                    "Authentication success",
                    Toast.LENGTH_LONG
            ).show()
            this@MFAActivity.isAuthenticated = true
            biometricReqOnRefresh.isEnabled = true
            launchWithBio.isEnabled = true

            val status = JWTUtils.isAccessTokenExpired(accessTokenData)
            Log.i(tag, "Access Token Status " + status)
            if(!status) {
                val account = setupAccount()
                getAccessTokenUsingRefreshToken(account)
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
     * Invoke security settings screen to register biometrics
     */
    private fun launchBiometricSetup() {
        this.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }

    /**
     * Show biometrics enrollment popup if not registered
     */
    private fun showBiometricsEnrollmentAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonwithType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.NEGATIVE) {
                    // User cancels dialog
                } else if (buttonType == AlertButtonType.POSITIVE) {
                    launchBiometricSetup()
                }
            }
        })
        enrollFingerPrintDlg.displayAlert(
                this,
                this.getString(R.string.dialog_header_text),
                this.getString(R.string.dialog_biometric_desc), false,
                mutableListOf(AlertButton("Cancel", AlertButtonType.NEGATIVE), AlertButton("OK", AlertButtonType.POSITIVE))
        )
    }

    /**
     * Show popup when access token expired
     */
    private fun showAccessTokenExpireAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonwithType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.NEGATIVE) {
                    // User cancels dialog
                } else if (buttonType == AlertButtonType.POSITIVE) {
                    showBiometric()
                }
            }
        })
        enrollFingerPrintDlg.displayAlert(
                this,
                this.getString(R.string.dialog_header_text),
                this.getString(R.string.dialog_access_token_expire_desc), false,
                mutableListOf(AlertButton("Cancel", AlertButtonType.NEGATIVE), AlertButton("OK", AlertButtonType.POSITIVE))
        )
    }

    /**
     * Show popup when refresh token expired
     */
    private fun showRefreshTokenExpireAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonwithType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.NEGATIVE) {
                    // User cancels dialog
                } else if (buttonType == AlertButtonType.POSITIVE) {
                    val account = setupAccount()
                    endSession(account)
                }
            }
        })
        enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.dialog_header_text),
            this.getString(R.string.dialog_refresh_token_expire_desc), false,
            mutableListOf(AlertButton("Cancel", AlertButtonType.NEGATIVE), AlertButton("OK", AlertButtonType.POSITIVE))
        )
    }
}