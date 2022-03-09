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
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cyberark.identity.util.dialog.AlertButton
import com.cyberark.identity.util.dialog.AlertButtonType
import com.cyberark.identity.util.dialog.AlertDialogButtonCallback
import com.cyberark.identity.util.dialog.AlertDialogHandler
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.utils.AppConfig
import com.cyberark.mfa.utils.PreferenceConstants
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import org.json.JSONException

import org.json.JSONArray

class NativeLoginActivity : AppCompatActivity() {

    companion object {
        const val TAG = "NativeLoginActivity"
    }

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar
    private lateinit var loginErrorAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_login)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        updateUI()
    }

    /**
     * Update UI for native login screen
     *
     */
    private fun updateUI() {
        // Invoke UI element
        progressBar = findViewById(R.id.progressBar_native_login_activity)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)

        val loginButton = findViewById<Button>(R.id.button_login)
        loginButton.setOnClickListener {
            hideKeyboard(loginButton)
            if (username.text.isBlank() || password.text.isBlank()) {
                showLoginErrorAlert()
            } else {
                performLogin(username.text.toString(), password.text.toString())
            }
        }
    }

    /**
     * Hide keyboard
     *
     * @param view: view instance
     */
    private fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    /**
     * Native login using username and password
     *
     * @param username: login username
     * @param password: login password
     */
    // TODO.. need to update header params based on api updates
    private fun performLogin(username: String, password: String) {
        // Show progress indicator
        progressBar.visibility = View.VISIBLE

        val baseUrl = AppConfig.getNativeLoginURL(this)
        // Native login URL
        val url = "$baseUrl/api/BasicLogin"

        // Body params
        val bodyParams = JSONObject()
        bodyParams.put("Username", username)
        bodyParams.put("Password", password)
        val requestBody = bodyParams.toString()

        // Network request object
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, null,
            Response.Listener { response ->
                try {
                    Log.d(TAG, response.toString())
//                    val headers = response.getJSONObject("headers")
//                    Log.d(TAG, "headers: $headers")
                    if (response.getBoolean("Success")) {
                        // Get sessionUuid and username from response object
                        val result = response.getJSONObject("Result")
                        val sessionUuid = result.getString("SessionUuid")
                        val usernameString = result.getString("MFAUserName")

                        // Save session token
                        KeyStoreProvider.get().saveSessionToken(sessionUuid)
                        // Save mfa widget username
                        CyberArkPreferenceUtil.putString(
                            PreferenceConstants.MFA_WIDGET_USERNAME,
                            usernameString
                        )
                        // Hide progress indicator
                        progressBar.visibility = View.GONE

                        // Start TransferFundActivity
                        val intent = Intent(this, TransferFundActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } catch (ex: Exception) {
                    // Hide progress indicator
                    progressBar.visibility = View.GONE
                    Log.d(TAG, "Error message: " + ex.message)
                    // Show login error message using Toast
                    showLoginErrorAlert()
                }
            },
            Response.ErrorListener { error ->
                // Hide progress indicator
                progressBar.visibility = View.GONE
                Log.d(TAG, "Error message: $error")
                // Show login error message using Toast
                showLoginErrorAlert()
            }) {

            override fun getBody(): ByteArray {
                return requestBody.encodeToByteArray()
            }

//            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
//                val token = response!!.headers!!["set-cookie"]
//                Log.d(TAG, "token: $token")
//
//                return try {
//                    val json = String(
//                        response?.data ?: ByteArray(0),
//                        Charset.forName(HttpHeaderParser.parseCharset(response?.headers)))
//                    val jsonResponse = JSONObject()
//                    jsonResponse.put("headers", JSONObject(json))
//
//                    Response.success(
//                        jsonResponse,
//                        HttpHeaderParser.parseCacheHeaders(response))
//                } catch (e: UnsupportedEncodingException) {
//                    Response.error(ParseError(e))
//                } catch (e: JsonSyntaxException) {
//                    Response.error(ParseError(e))
//                }
//            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            50000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(this).add(request)
    }

    // **************** Handle menu settings click action Start *********************** //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            //Start Settings activity
            val intent = Intent(this, NativeLoginSettingsActivity::class.java)
            intent.putExtra("from_activity", "NativeLoginActivity")
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    // **************** Handle menu settings click action End *********************** //

    private fun showLoginErrorAlert() {

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
}