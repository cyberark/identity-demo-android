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

package com.cyberark.mfa.scenario2

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.builder.CyberArkWidgetBuilder
import com.cyberark.identity.util.*
import com.cyberark.identity.util.biometric.CyberArkBiometricCallback
import com.cyberark.identity.util.biometric.CyberArkBiometricManager
import com.cyberark.identity.util.biometric.CyberArkBiometricPromptUtility
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.activity.WelcomeActivity
import com.cyberark.mfa.utils.AppConfig
import com.cyberark.mfa.utils.PreferenceConstants

class TransferFundActivity : AppCompatActivity() {

    private var biometricsOnAppLaunchRequested: Boolean = false
    private var biometricsOnTransferFundRequested: Boolean = false

    private lateinit var mfaWidgetBuilder: CyberArkWidgetBuilder
    private lateinit var editTextAmount: EditText
    private lateinit var enterAmount: TextView
    private lateinit var mfaWidgetUsername: String

    // SDK biometrics utility variable
    private lateinit var cyberArkBiometricPromptUtility: CyberArkBiometricPromptUtility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_fund)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        title = getString(R.string.acme)
        mfaWidgetBuilder = AppConfig.setupNativeLoginFromSharedPreference(this)
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
    }

    override fun onStop() {
        super.onStop()
        editTextAmount.text = null
    }

    private fun cleanUp() {
        //Remove session token
        CyberArkPreferenceUtil.remove(Constants.SESSION_TOKEN)
        CyberArkPreferenceUtil.remove(Constants.SESSION_TOKEN_IV)
        // Remove biometrics settings status
        CyberArkPreferenceUtil.remove(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL)
        CyberArkPreferenceUtil.remove(PreferenceConstants.INVOKE_BIOMETRICS_ON_TRANSFER_FUND_NL)
        CyberArkPreferenceUtil.remove(PreferenceConstants.MFA_WIDGET_USERNAME)
        CyberArkPreferenceUtil.apply()

        // Start HomeActivity
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Update UI for transfer fund screen
     *
     */
    private fun updateUI() {
        // Get the shared preference status and update the biometrics selection
        if (!CyberArkPreferenceUtil.contains(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL)) {
            CyberArkPreferenceUtil.putBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL,
                true
            )
            CyberArkPreferenceUtil.putBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_TRANSFER_FUND_NL,
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

        // Get the shared preference status and handle device biometrics on transfer fund
        biometricsOnTransferFundRequested =
            CyberArkPreferenceUtil.getBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_TRANSFER_FUND_NL,
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
            if (editTextAmount.text.trim().isEmpty()) {
                enterAmount.visibility = View.VISIBLE
            } else {
                if (biometricsOnTransferFundRequested) {
                    showBiometrics()
                } else {
                    val intent = Intent(this, MFAWidgetActivity::class.java)
                    intent.putExtra(
                        "MFA_WIDGET_URL",
                        mfaWidgetBuilder.getMFAWidgetBaseURL(mfaWidgetUsername)
                    )
                    startActivity(intent)
                }
            }
            editTextAmount.clearFocus()
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
            //Start Settings activity
            val intent = Intent(this, NativeLoginSettingsActivity::class.java)
            intent.putExtra("from_activity", "TransferFundActivity")
            startActivity(intent)
            true
        }
        R.id.action_logout -> {
            // Perform clean-up and logout
            cleanUp()
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
            false)
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
            } else if (biometricsOnTransferFundRequested) {
                val intent = Intent(this@TransferFundActivity, MFAWidgetActivity::class.java)
                intent.putExtra(
                    "MFA_WIDGET_URL",
                    mfaWidgetBuilder.getMFAWidgetBaseURL(mfaWidgetUsername)
                )
                startActivity(intent)
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
}