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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.ResponseStatus
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil

/**
 * Implementing SDK feature in HomeActivity
 * 1. OAuth 2.0 PKCE driven login flow
 *
 */
class HomeActivity : AppCompatActivity() {

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    // Login button variable
    private lateinit var logInButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Invoke UI element
        progressBar = findViewById(R.id.progressBar_home_activity)

        // Setup account
        val account = setupAccount()

        // Perform login
        logInButton = findViewById(R.id.button_log_in)
        logInButton.setOnClickListener {
            startAuthentication(account)
        }

        // Verify if access token is present or not
        val accessToken = KeyStoreProvider.get().getAuthToken()
        if (accessToken != null) {
            //Start MFA activity if access token is available
            val intent = Intent(this, MFAActivity::class.java)
            startActivity(intent)
            finish()
        }
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
            .domainURL(getString(R.string.cyberark_account_host))
            .clientId(getString(R.string.cyberark_account_client_id))
            .appId(getString(R.string.cyberark_account_app_id))
            .responseType(getString(R.string.cyberark_account_response_type))
            .scope(getString(R.string.cyberark_account_scope))
            .redirectUri(getString(R.string.cyberark_account_redirect_uri))
            .build()
        return cyberArkAccountBuilder
    }

    /**
     * Launch URL in browser, set-up view model, start authentication flow
     * and handle API response using active observer
     *
     * @param cyberArkAccountBuilder: CyberArkAccountBuilder instance
     */
    private fun startAuthentication(cyberArkAccountBuilder: CyberArkAccountBuilder) {
        val authResponseHandler: LiveData<ResponseHandler<AuthCodeFlowModel>> =
            CyberArkAuthProvider.login(cyberArkAccountBuilder).start(this)

        // Verify if there is any active observer, if not then add observer to get API response
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        // Show authentication success message using Toast
                        Toast.makeText(
                            this,
                            "Received Access Token & Refresh Token" + ResponseStatus.SUCCESS.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.i("Access Token", it.data!!.access_token)
                        // Save access token and refresh token in SharedPref using keystore encryption
                        KeyStoreProvider.get().saveAuthToken(it.data!!.access_token)
                        KeyStoreProvider.get().saveRefreshToken(it.data!!.refresh_token)
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Start MFAActivity
                        val intent = Intent(this, MFAActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    ResponseStatus.ERROR -> {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Show authentication error message using Toast
                        Toast.makeText(
                            this,
                            "Error: Unable to fetch Access Token & Refresh Token",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ResponseStatus.LOADING -> {
                        // Show progress indicator
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }
}