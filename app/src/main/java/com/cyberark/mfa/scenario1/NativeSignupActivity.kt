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

package com.cyberark.mfa.scenario1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cyberark.identity.util.dialog.AlertButton
import com.cyberark.identity.util.dialog.AlertButtonType
import com.cyberark.identity.util.dialog.AlertDialogButtonCallback
import com.cyberark.identity.util.dialog.AlertDialogHandler
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.scenario2.NativeLoginActivity
import com.cyberark.mfa.scenario2.TransferFundActivity
import com.cyberark.mfa.utils.AppConfig
import com.cyberark.mfa.utils.PreferenceConstants
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import org.json.JSONObject

class NativeSignupActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val TAG = "NativeSignupActivity"
        const val SITE_KEY = "6Lf9noAeAAAAAHDfOkMTljFc3uDC1HNu0zy1iPcP"
        const val SITE_SECRET_KEY = "6Lf9noAeAAAAAHjidiyCpgEfn_4ghsDkzEKuAaiz"
    }

    private lateinit var tvVerify: TextView
    private lateinit var btnverifyCaptcha: Button
    private lateinit var queue: RequestQueue

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var emailAddress: EditText
    private lateinit var mobileNumber: EditText
    private lateinit var signupButton: Button
    private lateinit var loginErrorAlert: AlertDialog
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_signup1)

        tvVerify = findViewById(R.id.textView)
        btnverifyCaptcha = findViewById(R.id.button)
        btnverifyCaptcha.setOnClickListener(this)
        queue = Volley.newRequestQueue(this)

//        invokeUI()
//        updateUI()
    }

    private fun invokeUI() {
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirm_password)
        emailAddress = findViewById(R.id.email_address)
        mobileNumber = findViewById(R.id.mobile_number)
        signupButton = findViewById(R.id.button_signup)
        progressBar = findViewById(R.id.progressBar_native_signup_activity)
    }

    private fun updateUI() {
        signupButton.setOnClickListener {
            hideKeyboard(signupButton)
            if (username.text.isBlank() || password.text.isBlank() || confirmPassword.text.isBlank()
                || emailAddress.text.isBlank() || mobileNumber.text.isBlank()) {
                showLoginErrorAlert()
            } else {
                performSignup(username.text.toString(), password.text.toString(),
                    confirmPassword.text.toString(), emailAddress.text.toString(),
                    mobileNumber.text.toString())
            }
        }
    }

    private fun performSignup(username: String, password: String, confirmPassword: String,
                              emailAddress: String, mobileNumber: String) {
      
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button -> {
                SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                    .addOnSuccessListener(this) { response ->
                        // Indicates communication with reCAPTCHA service was
                        // successful.
                        val userResponseToken = response.tokenResult
                        Log.e("response", userResponseToken!!)
                        if (userResponseToken.isNotEmpty()) {
                            Log.i(TAG, userResponseToken.toString())
                            // Validate the user response token using the
                            // reCAPTCHA siteverify API.
//                            handleVerify(response.tokenResult!!)
                        }
                    }
                    .addOnFailureListener(this) { e ->
                        if (e is ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            Log.d(
                                TAG,
                                ("Error message: " + CommonStatusCodes.getStatusCodeString(e.statusCode))
                            )
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d(TAG, "Unknown type of error: " + e.message)
                        }
                    }
            }
        }
    }

    private fun handleVerify(responseToken: String) {
        //it is google recaptcha site verify server
        //you can place your server url
        val url = "https://www.google.com/recaptcha/api/siteverify"

        val params: MutableMap<String, String> = HashMap()
        params["secret"] = SITE_SECRET_KEY
        params["response"] = responseToken

        val request: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        tvVerify.text = "You're not a Robot"
                    }
                } catch (ex: Exception) {
                    Log.d(TAG, "Error message: " + ex.message)
                }
            },
            Response.ErrorListener { error -> Log.d(TAG, "Error message: " + error.message) }) {

            override fun getParams(): MutableMap<String, String> {
                return params
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            50000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }
}