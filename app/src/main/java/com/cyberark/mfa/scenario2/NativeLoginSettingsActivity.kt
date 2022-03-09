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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.utils.PreferenceConstants

class NativeLoginSettingsActivity : AppCompatActivity() {

    private lateinit var nativeLoginURL: EditText
    private lateinit var mfaWidgetHostUrl: EditText
    private lateinit var mfaWidgetId: EditText

    // Device biometrics checkbox variables
    private lateinit var biometricsOnAppLaunchCheckbox: CheckBox
    private var biometricsOnAppLaunchRequested: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_login_settings)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        title = getString(R.string.settings)
        invokeUI()
        updateUI()
        val basicLoginURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.NATIVE_LOGIN_URL, null)
        if (basicLoginURLSP == null) {
            saveInSharedPreference()
        }
        verifyAndSaveInSharedPreference()
    }

    private fun invokeUI() {
        biometricsOnAppLaunchCheckbox = findViewById(R.id.biometrics_on_app_launch_checkbox)

        val beforeLoginLayout: LinearLayout = findViewById(R.id.before_login_layout)
        val afterLoginLayout: LinearLayout = findViewById(R.id.after_login_layout)
        val activityIntent = intent
        when {
            activityIntent.getStringExtra("from_activity").equals("NativeLoginActivity") -> {
                beforeLoginLayout.visibility = View.VISIBLE
                afterLoginLayout.visibility = View.GONE
            }
            activityIntent.getStringExtra("from_activity").equals("TransferFundActivity") -> {
                beforeLoginLayout.visibility = View.GONE
                afterLoginLayout.visibility = View.VISIBLE
            }
            else -> {
                beforeLoginLayout.visibility = View.GONE
                afterLoginLayout.visibility = View.GONE
            }
        }

        nativeLoginURL = findViewById(R.id.editTextBasicLoginURL)
        mfaWidgetHostUrl = findViewById(R.id.editTextMFAWidgetHostURL)
        mfaWidgetId = findViewById(R.id.editTextMFAWidgetId)
    }

    private fun updateUI() {
        nativeLoginURL.setText(getString(R.string.acme_native_login_url))
        mfaWidgetHostUrl.setText(getString(R.string.cyberark_widget_host_url))
        mfaWidgetId.setText(getString(R.string.cyberark_widget_id))

        // Get the shared preference status and handle device biometrics on app launch
        biometricsOnAppLaunchCheckbox.isChecked =
            CyberArkPreferenceUtil.getBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL,
                false
            )
        biometricsOnAppLaunchRequested = biometricsOnAppLaunchCheckbox.isChecked
        biometricsOnAppLaunchCheckbox.setOnClickListener {
            saveBiometricsRequestOnAppLaunch(biometricsOnAppLaunchCheckbox.isChecked)
        }

        // Get the shared preference status and update the biometrics selection
        if (!CyberArkPreferenceUtil.contains(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL)) {
            saveBiometricsRequestOnAppLaunch(true)
        }
    }

    private fun saveInSharedPreference() {
        CyberArkPreferenceUtil.putString(PreferenceConstants.NATIVE_LOGIN_URL, nativeLoginURL.text.toString())
        CyberArkPreferenceUtil.putString(PreferenceConstants.MFA_WIDGET_URL, mfaWidgetHostUrl.text.toString())
        CyberArkPreferenceUtil.putString(PreferenceConstants.MFA_WIDGET_ID, mfaWidgetId.text.toString())

        CyberArkPreferenceUtil.putBoolean(
            PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL,
            biometricsOnAppLaunchRequested
        )
    }

    private fun verifyAndSaveInSharedPreference() {
        val nativeLoginURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.NATIVE_LOGIN_URL, null)
        if (!nativeLoginURLSP.equals(nativeLoginURL.text.toString())) {
            nativeLoginURL.setText(nativeLoginURLSP)
        }
        val mfaWidgetURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.MFA_WIDGET_URL, null)
        if (!mfaWidgetURLSP.equals(mfaWidgetHostUrl.text.toString())) {
            mfaWidgetHostUrl.setText(mfaWidgetURLSP)
        }
        val mfaWidgetID = CyberArkPreferenceUtil.getString(PreferenceConstants.MFA_WIDGET_ID, null)
        if (!mfaWidgetID.equals(mfaWidgetId.text.toString())) {
            mfaWidgetId.setText(mfaWidgetID)
        }
    }

    // **************** Handle menu settings click action Start *********************** //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.save_settings_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            saveInSharedPreference()
            updateFromSettings()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun updateFromSettings() {
        val activityIntent = intent
        if (activityIntent.getStringExtra("from_activity").equals("NativeLoginActivity")) {
            val intent = Intent(this, NativeLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else if (activityIntent.getStringExtra("from_activity").equals("TransferFundActivity")) {
            val intent = Intent(this, TransferFundActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        finish()
    }
    // **************** Handle menu settings click action End *********************** //


    // ************************ Handle biometrics Start **************************** //
    /**
     * Save "Invoke biometrics on app launch" status in shared preference
     *
     * @param checked: Boolean
     */
    private fun saveBiometricsRequestOnAppLaunch(checked: Boolean) {
        var value = checked
        if (!CyberArkPreferenceUtil.contains(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL)) {
            value = true
        }
        biometricsOnAppLaunchRequested = value
    }
    // ************************ Handle biometrics End **************************** //

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}