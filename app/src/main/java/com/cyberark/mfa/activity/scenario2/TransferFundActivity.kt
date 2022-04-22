/*
 * Copyright (c) 2022 CyberArk Software Ltd. All rights reserved.
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

package com.cyberark.mfa.activity.scenario2

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.cyberark.identity.builder.CyberArkMFAWidgetBuilder
import com.cyberark.identity.util.*
import com.cyberark.identity.util.biometric.CyberArkBiometricCallback
import com.cyberark.identity.util.biometric.CyberArkBiometricManager
import com.cyberark.identity.util.biometric.CyberArkBiometricPromptUtility
import com.cyberark.identity.util.dialog.AlertButton
import com.cyberark.identity.util.dialog.AlertButtonType
import com.cyberark.identity.util.dialog.AlertDialogButtonCallback
import com.cyberark.identity.util.dialog.AlertDialogHandler
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.activity.WelcomeActivity
import com.cyberark.mfa.activity.base.BaseActivity
import com.cyberark.mfa.utils.AppConfig
import com.cyberark.mfa.utils.PreferenceConstants

class TransferFundActivity : BaseActivity() {

    companion object {
        const val TAG = "TransferFundActivity"
        const val ON_APP_RESUME = "ON_APP_RESUME"
        const val ON_SETTINGS_CLICK = "ON_SETTINGS_CLICK"
        const val ON_TRANSFER_FUND = "ON_TRANSFER_FUND"
    }

    private var biometricsOnAppLaunchRequested: Boolean = false
    private lateinit var mfaMFAWidgetBuilder: CyberArkMFAWidgetBuilder
    private lateinit var editTextAmount: EditText
    private lateinit var enterAmount: TextView
    private lateinit var mfaWidgetUsername: String
    private lateinit var queue: RequestQueue

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionTimeoutErrorAlert: AlertDialog

    // SDK biometrics utility variable
    private lateinit var cyberArkBiometricPromptUtility: CyberArkBiometricPromptUtility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_fund)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        title = getString(R.string.acme)

        queue = Volley.newRequestQueue(this)
        mfaMFAWidgetBuilder = AppConfig.setupNativeLoginFromSharedPreference(this)
        mfaWidgetUsername =
            CyberArkPreferenceUtil.getString(PreferenceConstants.MFA_WIDGET_USERNAME, null)
                .toString()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        if (biometricsOnAppLaunchRequested) {
            showBiometrics()
        }
        // Verify session timeout using HeartBeat API
        callHeartBeatAPI(ON_APP_RESUME)
    }

    override fun onStop() {
        super.onStop()
        editTextAmount.text = null
    }

    /**
     * Update UI for transfer fund screen
     *
     */
    private fun updateUI() {
        // Invoke UI element
        progressBar = findViewById(R.id.progressBar_transfer_fund_activity)

        // Get the shared preference status and update the biometrics selection
        if (!CyberArkPreferenceUtil.contains(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL)) {
            CyberArkPreferenceUtil.putBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL,
                true
            )
        }

        // Invoke biometric utility instance
        cyberArkBiometricPromptUtility =
            CyberArkBiometricManager().getBiometricUtility(biometricCallback)

        // Get the shared preference status and handle device biometrics on app launch
        biometricsOnAppLaunchRequested =
            CyberArkPreferenceUtil.getBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL,
                false
            )

        setupHyperlink()
        enterAmount = findViewById(R.id.enter_amount)
        editTextAmount = findViewById(R.id.edit_text_amount)

        // set edit text focus change listener
        editTextAmount.onFocusChangeListener = View.OnFocusChangeListener { _, status ->
            if (status) {
                enterAmount.visibility = View.GONE
            }
        }
        findViewById<Button>(R.id.button_transfer_funds).setOnClickListener {
            // Verify session timeout using HeartBeat API
            callHeartBeatAPI(ON_TRANSFER_FUND)
        }
    }

    private fun setupHyperlink() {
        val apiDocsMFAWidgetView: TextView = findViewById(R.id.api_doc_mfa_widget)
        apiDocsMFAWidgetView.movementMethod = LinkMovementMethod.getInstance()
        val apiDocsView: TextView = findViewById(R.id.api_doc)
        apiDocsView.movementMethod = LinkMovementMethod.getInstance()
    }

    // **************** Handle menu settings click action Start *********************** //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.logout_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // Verify session timeout using HeartBeat API
            callHeartBeatAPI(ON_SETTINGS_CLICK)
            true
        }
        R.id.action_logout -> {
            // Perform clean-up and logout
            performNativeLogout(progressBar)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    // **************** Handle menu settings click action End *********************** //


    // ************************ Handle biometrics Start **************************** //
    /**
     * Show all strong biometrics in a prompt
     * negativeButtonText: "Use App Pin" text in order to handle fallback scenario
     * useDevicePin: true/false (true when biometrics is integrated with device pin as fallback else false)
     *
     */
    private fun showBiometrics() {
        cyberArkBiometricPromptUtility.showBioAuthentication(
            this,
            null,
            "Use App Pin",
            false
        )
    }

    /**
     * Callback to handle biometrics response
     */
    private val biometricCallback = object : CyberArkBiometricCallback {
        override fun isAuthenticationSuccess(success: Boolean) {
            // Show Authentication success message using Toast
            Toast.makeText(
                this@TransferFundActivity,
                getString(R.string.authentication_is_successful),
                Toast.LENGTH_LONG
            ).show()

            if (biometricsOnAppLaunchRequested) {
                biometricsOnAppLaunchRequested = false
            }
        }

        override fun passwordAuthenticationSelected() {
            Toast.makeText(
                this@TransferFundActivity,
                "Password authentication is selected",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun showErrorMessage(message: String) {
            Toast.makeText(this@TransferFundActivity, message, Toast.LENGTH_LONG).show()
        }

        override fun isHardwareSupported(boolean: Boolean) {
            if (!boolean) {
                Toast.makeText(
                    this@TransferFundActivity,
                    "Hardware is not supported",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun isSdkVersionSupported(boolean: Boolean) {
            Toast.makeText(
                this@TransferFundActivity,
                "SDK version is not supported",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun isBiometricEnrolled(boolean: Boolean) {
            if (!boolean) {
                Toast.makeText(
                    this@TransferFundActivity,
                    "Biometric is not enrolled",
                    Toast.LENGTH_LONG
                ).show()
                // Show biometric enrollment alert popup
                showBiometricsEnrollmentAlert()
            }
        }

        override fun biometricErrorSecurityUpdateRequired() {
            Toast.makeText(
                this@TransferFundActivity,
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

    /**
     * Call heart beat API to get active session status
     *
     * @param actionName: action initiated name
     */
    private fun callHeartBeatAPI(actionName: String) {
        handleSessionTimeout(this, progressBar, actionName)
    }

    /**
     * Notify session timeout status using HeartBeat API Response,
     * show error alert if the session is expired, and perform the clean-up and logout
     *
     * @param status: session timeout status
     * @param actionName: action name
     */
    fun notifySessionTimeoutStatus(status: Boolean, actionName: String) {
        if(!status) {
            showSessionTimeoutErrorAlert()
        } else {
            when {
                actionName.equals(ON_APP_RESUME) -> {
                    // do nothing
                }
                actionName.equals(ON_SETTINGS_CLICK) -> {
                    //Start Settings activity
                    val intent = Intent(this, NativeLoginSettingsActivity::class.java)
                    intent.putExtra("from_activity", "TransferFundActivity")
                    startActivity(intent)
                }
                actionName.equals(ON_TRANSFER_FUND) -> {
                    if (editTextAmount.text.trim().isEmpty()) {
                        enterAmount.visibility = View.VISIBLE
                    } else {
                        val intent = Intent(this, MFAWidgetActivity::class.java)
                        intent.putExtra(
                            "MFA_WIDGET_URL",
                            mfaMFAWidgetBuilder.getMFAWidgetBaseURL(mfaWidgetUsername)
                        )
                        startActivity(intent)
                    }
                    editTextAmount.clearFocus()
                }
            }
        }
    }

    /**
     * Show session timeout error alert
     *
     */
    private fun showSessionTimeoutErrorAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.POSITIVE) {
                    // User cancels dialog
                    sessionTimeoutErrorAlert.dismiss()
                    cleanUpForSessionTimeout()
                }
            }
        })
        sessionTimeoutErrorAlert = enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.dialog_session_timeout_error_header_text),
            this.getString(R.string.dialog_session_timeout_error_desc), false,
            mutableListOf(
                AlertButton("OK", AlertButtonType.POSITIVE)
            )
        )
    }

    /**
     * Perform clean-up for Session Timeout
     *
     */
    private fun cleanUpForSessionTimeout() {
        //Remove session token
        CyberArkPreferenceUtil.remove(Constants.SESSION_TOKEN)
        CyberArkPreferenceUtil.remove(Constants.SESSION_TOKEN_IV)
        //Remove header token
        CyberArkPreferenceUtil.remove(Constants.HEADER_TOKEN)
        CyberArkPreferenceUtil.remove(Constants.HEADER_TOKEN_IV)
        // Remove biometrics settings status
        CyberArkPreferenceUtil.remove(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL)
        CyberArkPreferenceUtil.remove(PreferenceConstants.MFA_WIDGET_USERNAME)
        CyberArkPreferenceUtil.apply()

        // Start HomeActivity
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}