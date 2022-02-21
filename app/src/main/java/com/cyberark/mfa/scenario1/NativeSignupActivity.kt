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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.SignupCaptchaModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.*
import com.cyberark.identity.util.dialog.AlertButton
import com.cyberark.identity.util.dialog.AlertButtonType
import com.cyberark.identity.util.dialog.AlertDialogButtonCallback
import com.cyberark.identity.util.dialog.AlertDialogHandler
import com.cyberark.mfa.R
import com.cyberark.mfa.activity.SettingsActivity
import com.cyberark.mfa.utils.AppConfig
import org.json.JSONObject

class NativeSignupActivity : AppCompatActivity() {

    companion object {
        const val TAG = "NativeSignupActivity"
        // Google reCaptcha V2 Site Key
        const val SITE_KEY = "6Lf9noAeAAAAAHDfOkMTljFc3uDC1HNu0zy1iPcP"
    }

    private lateinit var loginErrorAlert: AlertDialog
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_signup)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        updateUI()
    }

    /**
     * Update UI components
     *
     */
    private fun updateUI() {

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val confirmPassword = findViewById<EditText>(R.id.confirm_password)
        val emailAddress = findViewById<EditText>(R.id.email_address)
        val mobileNumber = findViewById<EditText>(R.id.mobile_number)
        val signupButton = findViewById<Button>(R.id.button_signup)
        progressBar = findViewById(R.id.progressBar_native_signup_activity)

        signupButton.setOnClickListener {
            hideKeyboard(signupButton)
            if (username.text.isBlank() || password.text.isBlank() || confirmPassword.text.isBlank()
                || emailAddress.text.isBlank() || mobileNumber.text.isBlank()) {
                showLoginErrorAlert()
            } else {
                val signupData = JSONObject()
                signupData.put("Name", username.text.toString())
                signupData.put("Password", password.text.toString())
                signupData.put("ConfirmPassword", confirmPassword.text.toString())
                signupData.put("Mail", emailAddress.text.toString())
                signupData.put("MobileNumber", mobileNumber.text.toString())
                // Setup account
                val account =  AppConfig.setupAccountFromSharedPreference(this)
                performSignup(account, signupData)
            }
        }
    }

    /**
     * Perform signup
     *
     * @param cyberArkAccountBuilder: CyberArkAccountBuilder instance
     * @param signupData: signup form data
     */
    private fun performSignup(cyberArkAccountBuilder: CyberArkAccountBuilder, signupData: JSONObject) {
        val signupResponseHandler: LiveData<ResponseHandler<SignupCaptchaModel>> =
            CyberArkAuthProvider.signupWithCaptcha(cyberArkAccountBuilder).start(this, signupData, SITE_KEY)

        // Verify if there is any active observer, if not then add observer to get API response
        if (!signupResponseHandler.hasActiveObservers()) {
            signupResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        // Show enrollment success message using Toast
                        if(it.data!!.success) {
                            Toast.makeText(
                                this,
                                "Signup has completed successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Signup has failed :: " + it.data!!.Message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                    }
                    ResponseStatus.ERROR -> {
                        // Show enrollment error message using Toast
                        Toast.makeText(
                            this,
                            "Error: Unable to complete signup",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
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
     * Show login error alert
     *
     */
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
            this.getString(R.string.dialog_signup_error_header_text),
            this.getString(R.string.dialog_signup_error_desc), true,
            mutableListOf(
                AlertButton("OK", AlertButtonType.POSITIVE)
            )
        )
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
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("from_activity", "NativeSignupActivity")
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    // **************** Handle menu settings click action End *********************** //
}