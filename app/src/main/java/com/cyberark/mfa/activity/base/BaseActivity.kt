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

package com.cyberark.mfa.activity.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.builder.CyberArkAuthWidgetBuilder
import com.cyberark.identity.data.model.AuthCodeFlowModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.ResponseStatus
import com.cyberark.identity.util.dialog.AlertButton
import com.cyberark.identity.util.dialog.AlertButtonType
import com.cyberark.identity.util.dialog.AlertDialogButtonCallback
import com.cyberark.identity.util.dialog.AlertDialogHandler
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.Constants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.activity.WelcomeActivity
import com.cyberark.mfa.activity.scenario1.MFAActivity
import com.cyberark.mfa.activity.scenario2.NativeLoginActivity
import com.cyberark.mfa.activity.scenario2.TransferFundActivity
import com.cyberark.mfa.utils.AppConfig
import com.cyberark.mfa.utils.PreferenceConstants
import org.json.JSONObject

open class BaseActivity : AppCompatActivity() {

    private lateinit var loginErrorAlert: AlertDialog
    private lateinit var logoutErrorAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Launch URL in browser, set-up view model, start authentication flow
     * and handle API response using active observer
     *
     * @param cyberArkAccountBuilder: CyberArkAccountBuilder instance
     * @param progressBar: ProgressBar instance
     */
    protected fun performCyberArkHostedLogin(
        cyberArkAccountBuilder: CyberArkAccountBuilder,
        progressBar: ProgressBar
    ) {
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
                            getString(R.string.access_token_and_refresh_token_received),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Save access token and refresh token in SharedPref using keystore encryption
                        KeyStoreProvider.get().saveAuthToken(it.data!!.access_token)
                        KeyStoreProvider.get().saveRefreshToken(it.data!!.refresh_token)
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Start MFAActivity
                        val intent = Intent(this, MFAActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    ResponseStatus.ERROR -> {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        if (it.message?.contains("access denied") == true) {
                            CyberArkAuthProvider.endSession(cyberArkAccountBuilder).start(this)
                            // Show authentication access denied error message using Toast
                            Toast.makeText(
                                this,
                                it.message,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // Show authentication generic error message using Toast
                            Toast.makeText(
                                this,
                                "Error: Unable to fetch Access Token & Refresh Token",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    ResponseStatus.LOADING -> {
                        // Show progress indicator
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    /**
     * Launch URL in browser, set-up view model, start authentication widget flow
     * and handle response using active observer
     *
     * @param cyberArkAccountBuilder: CyberArkAccountBuilder instance
     * @param cyberArkAuthWidgetBuilder: CyberArkAuthWidgetBuilder instance
     * @param progressBar: ProgressBar instance
     */
    protected fun performAuthenticationWidgetLogin(
        cyberArkAccountBuilder: CyberArkAccountBuilder,
        cyberArkAuthWidgetBuilder: CyberArkAuthWidgetBuilder,
        progressBar: ProgressBar
    ) {
        val authResponseHandler: LiveData<ResponseHandler<String>> =
            CyberArkAuthProvider.authWidgetLogin(cyberArkAccountBuilder)
                .start(this, cyberArkAuthWidgetBuilder)

        // Verify if there is any active observer, if not then add observer to get response
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        Log.i("LoginOptionActivity", it.data.toString())
                        // Initiate authorize URL using CyberArk hosted login
                        performCyberArkHostedLogin(cyberArkAccountBuilder, progressBar)
                    }
                    ResponseStatus.ERROR -> {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Show authentication generic error message using Toast
                        Toast.makeText(this, it.data.toString(), Toast.LENGTH_LONG).show()
                    }
                    ResponseStatus.LOADING -> {
                        // Show progress indicator
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    /**
     * Native login using username and password
     *
     * @param username: login username
     * @param password: login password
     * @param progressBar: ProgressBar instance
     */
    protected fun performNativeLogin(username: String, password: String, progressBar: ProgressBar) {
        // Show progress indicator
        progressBar.visibility = View.VISIBLE

        val baseUrl = AppConfig.getNativeLoginURL(this)
        // Native Login URL
        val url = "$baseUrl/api/BasicLogin"

        // Body params
        val bodyParams = JSONObject()
        bodyParams.put("Username", username)
        bodyParams.put("Password", password)
        val requestBody = bodyParams.toString()

        // Native Login Response header token
        var headerToken: String? = null

        // Network request object
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, null,
            Response.Listener { response ->
                try {
                    if (response.getBoolean("Success")) {
                        // Get sessionUuid and username from response object
                        val result = response.getJSONObject("Result")
                        val sessionUuid = result.getString("SessionUuid")
                        val usernameString = result.getString("MFAUserName")

                        // Save session token
                        KeyStoreProvider.get().saveSessionToken(sessionUuid)
                        // Save header token
                        KeyStoreProvider.get().saveHeaderToken(headerToken!!)
                        // Save mfa widget username
                        CyberArkPreferenceUtil.putString(
                            PreferenceConstants.MFA_WIDGET_USERNAME,
                            usernameString
                        )
                        // Hide progress indicator
                        progressBar.visibility = View.GONE

                        // Start TransferFundActivity
                        val intent = Intent(this, TransferFundActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Show login error message
                        showLoginErrorAlert()
                    }
                } catch (ex: Exception) {
                    // Hide progress indicator
                    progressBar.visibility = View.GONE
                    Log.d(NativeLoginActivity.TAG, "Error message: " + ex.message)
                    // Show login error message
                    showLoginErrorAlert()
                }
            },
            Response.ErrorListener { error ->
                // Hide progress indicator
                progressBar.visibility = View.GONE
                Log.d(NativeLoginActivity.TAG, "Error message: $error")
                // Show login error message
                showLoginErrorAlert()
            }) {

            override fun getBody(): ByteArray {
                return requestBody.encodeToByteArray()
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
                // Parse network response and capture "XSRF-TOKEN" from response header
                val token = response!!.headers!!["set-cookie"]
                headerToken = token?.removePrefix("XSRF-TOKEN=")?.removeSuffix("; Path=/")
                return super.parseNetworkResponse(response)
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            50000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(this).add(request)
    }

    /**
     * Perform native logout
     *
     * @param progressBar: ProgressBar instance
     */
    protected fun performNativeLogout(progressBar: ProgressBar) {
        // Show progress indicator
        progressBar.visibility = View.VISIBLE

        val baseUrl = AppConfig.getNativeLoginURL(this)
        // Native Logout URL
        val url = "$baseUrl/api/auth/logoutSession"

        // Get header token and session token using KeyStoreProvider
        val headerToken: String? = KeyStoreProvider.get().getHeaderToken()
        val sessionUuid = KeyStoreProvider.get().getSessionToken()

        // Header params
        val headerParams: MutableMap<String, String> = HashMap()
        headerParams["Cookie"] = "flow=flow3;XSRF-TOKEN=$headerToken;"
        headerParams["X-XSRF-TOKEN"] = headerToken!!
        headerParams["Content-Type"] = "application/json"

        // Body params
        val bodyParams = JSONObject()
        bodyParams.put("SessionUuid", sessionUuid)
        val requestBody = bodyParams.toString()

        // Network request object
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, null,
            Response.Listener { response ->
                try {
                    if (response.getBoolean("Success")) {
                        // Perform clean-up and logout
                        cleanUpForNativeLogout(progressBar)
                    } else {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Show logout error message
                        showLogoutErrorAlert()
                    }

                } catch (ex: Exception) {
                    // Hide progress indicator
                    progressBar.visibility = View.GONE
                    Log.d(TransferFundActivity.TAG, "Error message: " + ex.message)
                    // Show logout error message
                    showLogoutErrorAlert()
                }
            },
            Response.ErrorListener { error ->
                // Hide progress indicator
                progressBar.visibility = View.GONE
                Log.d(TransferFundActivity.TAG, "Error message: $error")
                // Show logout error message
                showLogoutErrorAlert()
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                return headerParams
            }

            override fun getBody(): ByteArray {
                return requestBody.encodeToByteArray()
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            50000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(this).add(request)
    }

    /**
     * Handle session timeout
     *
     * @param context: Activity Context
     * @param progressBar: ProgressBar instance
     * @param actionName: action name
     */
    protected fun handleSessionTimeout(
        context: Context,
        progressBar: ProgressBar,
        actionName: String
    ) {
        progressBar.visibility = View.VISIBLE

        val baseUrl = AppConfig.getNativeLoginURL(this)
        // Native Logout URL
        val url = "$baseUrl/api/HeartBeat"

        // Get header token and session token using KeyStoreProvider
        val headerToken: String? = KeyStoreProvider.get().getHeaderToken()
        val sessionUuid = KeyStoreProvider.get().getSessionToken()

        // Header params
        val headerParams: MutableMap<String, String> = HashMap()
        headerParams["Cookie"] = "flow=flow3;XSRF-TOKEN=$headerToken;"
        headerParams["X-XSRF-TOKEN"] = headerToken!!
        headerParams["Content-Type"] = "application/json"

        // Body params
        val bodyParams = JSONObject()
        bodyParams.put("SessionUuid", sessionUuid)
        val requestBody = bodyParams.toString()

        // Network request object
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, null,
            Response.Listener { response ->
                try {
                    val sessionTimeoutStatus = response.getBoolean("Success")
                    (context as TransferFundActivity?)!!.notifySessionTimeoutStatus(
                        sessionTimeoutStatus,
                        actionName
                    )
                    // Hide progress indicator
                    progressBar.visibility = View.GONE

                } catch (ex: Exception) {
                    // Hide progress indicator
                    progressBar.visibility = View.GONE
                    Log.d(TransferFundActivity.TAG, "Error message: " + ex.message)
                    (context as TransferFundActivity?)!!.notifySessionTimeoutStatus(
                        false,
                        actionName
                    )
                }
            },
            Response.ErrorListener { error ->
                // Hide progress indicator
                progressBar.visibility = View.GONE
                Log.d(TransferFundActivity.TAG, "Error message: $error")
                (context as TransferFundActivity?)!!.notifySessionTimeoutStatus(false, actionName)
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                return headerParams
            }

            override fun getBody(): ByteArray {
                return requestBody.encodeToByteArray()
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            50000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(this).add(request)
    }

    /**
     * Show login error alert
     *
     */
    protected fun showLoginErrorAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.POSITIVE) {
                    // User cancels dialog
                    loginErrorAlert.dismiss()
                }
            }
        })
        loginErrorAlert = enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.dialog_login_error_header_text),
            this.getString(R.string.dialog_login_error_desc), true,
            mutableListOf(
                AlertButton("OK", AlertButtonType.POSITIVE)
            )
        )
    }

    /**
     * Show logout error alert
     *
     */
    private fun showLogoutErrorAlert() {

        val enrollFingerPrintDlg = AlertDialogHandler(object : AlertDialogButtonCallback {
            override fun tappedButtonType(buttonType: AlertButtonType) {
                if (buttonType == AlertButtonType.POSITIVE) {
                    // User cancels dialog
                    logoutErrorAlert.dismiss()
                }
            }
        })
        logoutErrorAlert = enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.dialog_logout_error_header_text),
            this.getString(R.string.dialog_logout_error_desc), true,
            mutableListOf(
                AlertButton("OK", AlertButtonType.POSITIVE)
            )
        )
    }

    /**
     * Perform clean-up for Native Logout
     *
     */
    private fun cleanUpForNativeLogout(progressBar: ProgressBar) {
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

        // Hide progress indicator
        progressBar.visibility = View.GONE

        // Start HomeActivity
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }
}