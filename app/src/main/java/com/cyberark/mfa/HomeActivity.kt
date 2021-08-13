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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.cyberark.identity.builder.CyberarkAccountBuilder
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.provider.CyberarkAuthProvider
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.ResponseStatus
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.CyberarkPreferenceUtils

class HomeActivity : AppCompatActivity() {

    private val tag: String? = HomeActivity::class.simpleName

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    // Login button variable
    private lateinit var logInButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Invoke UI element
        progressBar = findViewById(R.id.progressBar_home_activity)

        // OAuth Authorization Code Flow + PKCE
        val account = setupAccount()

        // Perform login
        logInButton = findViewById(R.id.button_log_in)
        logInButton.setOnClickListener {
            startAuthentication(account)
        }

        // Verify if access token is present or not
        CyberarkPreferenceUtils.init(this)
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
     * Launch URL in browser, set-up view model and start authentication flow
     */
    private fun startAuthentication(cyberarkAccountBuilder: CyberarkAccountBuilder) {
        val authResponseHandler: LiveData<ResponseHandler<AuthCodeFlowModel>> =
                CyberarkAuthProvider.login(cyberarkAccountBuilder).start(this)

        // Verify if there is any active observer, if not then add observe to get API response
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {

                        //TODO.. need to verify and remove all logs
                        Log.i(tag, ResponseStatus.SUCCESS.toString())
                        Log.i(tag, it.data.toString())
                        Log.i(tag, it.data!!.access_token)
                        Log.i(tag, it.data!!.refresh_token)

                        Toast.makeText(
                                this,
                                "Received Access Token & Refresh Token",
                                Toast.LENGTH_SHORT
                        ).show()

                        val accessTokenData: String = it.data!!.access_token
                        val refreshTokenData: String = it.data!!.refresh_token

                        progressBar.visibility = View.GONE

                        //Save access token and refresh token in keystore
                        KeyStoreProvider.get().saveAuthToken(accessTokenData)
                        KeyStoreProvider.get().saveRefreshToken(refreshTokenData)

                        //Start MFA activity
                        val intent = Intent(this, MFAActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    ResponseStatus.ERROR -> {
                        progressBar.visibility = View.GONE
                        Log.i(tag, ResponseStatus.ERROR.toString())
                        Toast.makeText(
                                this,
                                "Error: Unable to fetch Access Token & Refresh Token",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    ResponseStatus.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }
}