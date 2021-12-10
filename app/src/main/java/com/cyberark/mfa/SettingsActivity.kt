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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.utils.PreferenceConstants

class SettingsActivity : AppCompatActivity() {

    private lateinit var systemURL: EditText
    private lateinit var hostURL: EditText
    private lateinit var clientId: EditText
    private lateinit var appId: EditText
    private lateinit var responseType: EditText
    private lateinit var scope: EditText
    private lateinit var redirectUri: EditText
    private lateinit var host: EditText
    private lateinit var scheme: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        title = getString(R.string.settings)
        invokeUI()
        updateUI()
        val systemURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.SYSTEM_URL, null)
        if (systemURLSP == null) {
            saveInSharedPreference()
        }
        verifyAndSaveInSharedPreference()
    }

    private fun invokeUI() {
        systemURL = findViewById(R.id.editTextSystemURL)
        hostURL = findViewById(R.id.editTextHostURL)
        clientId = findViewById(R.id.editTextClientId)
        appId = findViewById(R.id.editTextAppId)
        responseType = findViewById(R.id.editTextResponseType)
        responseType.isEnabled = false
        scope = findViewById(R.id.editTextScope)
        scope.isEnabled = false
        redirectUri = findViewById(R.id.editTextRedirectURI)
        host = findViewById(R.id.editTextHost)
        scheme = findViewById(R.id.editTextScheme)
    }

    private fun updateUI() {
        systemURL.setText(getString(R.string.cyberark_account_system_url))
        hostURL.setText(getString(R.string.cyberark_account_host_url))
        clientId.setText(getString(R.string.cyberark_account_client_id))
        appId.setText(getString(R.string.cyberark_account_app_id))
        responseType.setText(getString(R.string.cyberark_account_response_type))
        scope.setText(getString(R.string.cyberark_account_scope))
        redirectUri.setText(getString(R.string.cyberark_account_redirect_uri))
        host.setText(getString(R.string.cyberark_account_host))
        scheme.setText(getString(R.string.cyberark_account_scheme))
    }

    private fun saveInSharedPreference() {
        CyberArkPreferenceUtil.putString(PreferenceConstants.SYSTEM_URL, systemURL.text.toString())
        CyberArkPreferenceUtil.putString(PreferenceConstants.HOST_URL, hostURL.text.toString())
        CyberArkPreferenceUtil.putString(PreferenceConstants.CLIENT_ID, clientId.text.toString())
        CyberArkPreferenceUtil.putString(PreferenceConstants.APP_ID, appId.text.toString())
        CyberArkPreferenceUtil.putString(
            PreferenceConstants.REDIRECT_URI,
            redirectUri.text.toString()
        )
        CyberArkPreferenceUtil.putString(PreferenceConstants.HOST, host.text.toString())
        CyberArkPreferenceUtil.putString(PreferenceConstants.SCHEME, scheme.text.toString())
    }

    private fun verifyAndSaveInSharedPreference() {
        val systemURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.SYSTEM_URL, null)
        if (!systemURLSP.equals(systemURL.text.toString())) {
            systemURL.setText(systemURLSP)
        }
        val hostURLSP = CyberArkPreferenceUtil.getString(PreferenceConstants.HOST_URL, null)
        if (!hostURLSP.equals(hostURL.text.toString())) {
            hostURL.setText(hostURLSP)
        }
        val clientIdSP = CyberArkPreferenceUtil.getString(PreferenceConstants.CLIENT_ID, null)
        if (!clientIdSP.equals(clientId.text.toString())) {
            clientId.setText(clientIdSP)
        }
        val appIdSP = CyberArkPreferenceUtil.getString(PreferenceConstants.APP_ID, null)
        if (!appIdSP.equals(appId.text.toString())) {
            appId.setText(appIdSP)
        }
        val redirectUriSP = CyberArkPreferenceUtil.getString(PreferenceConstants.REDIRECT_URI, null)
        if (!redirectUriSP.equals(redirectUri.text.toString())) {
            redirectUri.setText(redirectUriSP)
        }
        val hostSP = CyberArkPreferenceUtil.getString(PreferenceConstants.HOST, null)
        if (!hostSP.equals(host.text.toString())) {
            host.setText(hostSP)
        }
        val schemeSP = CyberArkPreferenceUtil.getString(PreferenceConstants.SCHEME, null)
        if (!schemeSP.equals(scheme.text.toString())) {
            scheme.setText(schemeSP)
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
        if (activityIntent.getStringExtra("from_activity").equals("HomeActivity")) {
            val intent = Intent(this, LoginOptionsActivity::class.java)
            startActivity(intent)
        } else if (activityIntent.getStringExtra("from_activity").equals("MFAActivity")) {
            val intent = Intent(this, MFAActivity::class.java)
            startActivity(intent)
        } else if (activityIntent.getStringExtra("from_activity").equals("LoginOptionsActivity")) {
//            val intent = Intent(this, LoginOptionsActivity::class.java)
//            startActivity(intent)
        }
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateFromSettings()
    }
    // **************** Handle menu settings click action End *********************** //
}