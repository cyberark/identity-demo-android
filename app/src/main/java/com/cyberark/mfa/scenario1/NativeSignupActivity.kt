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
import android.text.TextUtils
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.SignupCaptchaModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.ResponseHandler
import com.cyberark.identity.util.ResponseStatus
import com.cyberark.mfa.R
import com.cyberark.mfa.activity.PopupActivity
import com.cyberark.mfa.activity.SettingsActivity
import com.cyberark.mfa.utils.AppConfig
import org.json.JSONObject
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * If the user has not registered before, the user will need to signup.
 * A user account will be created for the user in CyberArk Identity on registration
 *
 */
class NativeSignupActivity : AppCompatActivity() {

    companion object {
        const val TAG = "NativeSignupActivity"

        // Google reCaptcha V2 Site Key
        const val SITE_KEY = "6Lf9noAeAAAAAHDfOkMTljFc3uDC1HNu0zy1iPcP"
        const val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}\$"
    }

    private lateinit var progressBar: ProgressBar

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var emailAddress: EditText
    private lateinit var mobileNumber: EditText
    private val pattern: Pattern = Pattern.compile(PASSWORD_PATTERN)

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

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirm_password)
        emailAddress = findViewById(R.id.email_address)
        mobileNumber = findViewById(R.id.mobile_number)
        val signupButton = findViewById<Button>(R.id.button_signup)
        progressBar = findViewById(R.id.progressBar_native_signup_activity)

        // Submit signup form data
        signupButton.setOnClickListener {
            hideKeyboard(signupButton)
            if (checkAllFields()) {
                val signupData = JSONObject()
                signupData.put("Name", username.text.toString())
                signupData.put("Password", password.text.toString())
                signupData.put("ConfirmPassword", confirmPassword.text.toString())
                signupData.put("Mail", emailAddress.text.toString())
                signupData.put("MobileNumber", mobileNumber.text.toString())
                // Setup account
                val account = AppConfig.setupAccountFromSharedPreference(this)
                val siteKey = AppConfig.getSiteKey(this)
                performSignup(account, signupData, siteKey)
            }
        }
    }

    /**
     * Validate username, password, confirm password, email address before submitting to CyberArk Identity
     *
     * @return Boolean
     */
    private fun checkAllFields(): Boolean {
        var isValid = true
        if (isEmpty(username)) {
            username.error = "Username is required"
            username.requestFocus()
            isValid = false
        }
        if (isEmpty(password)) {
            password.error = "Password is required"
            if (isValid) {
                password.requestFocus()
                isValid = false
            }
        } else if (!isValidPassword(password.text.toString())) {
            password.error =
                "Password must be at least 8 character long " +
                        "with a combination of at least 1 uppercase, 1 lowercase and 1 number"
            if (isValid) {
                password.requestFocus()
                isValid = false
            }
        }
        if (isEmpty(confirmPassword)) {
            confirmPassword.error = "Confirm Password is required"
            if (isValid) {
                confirmPassword.requestFocus()
                isValid = false
            }
        } else if (password.text.toString() != confirmPassword.text.toString()) {
            confirmPassword.error = "Confirm Password must match with the Password"
            if (isValid) {
                confirmPassword.requestFocus()
                isValid = false
            }
        }
        if (isEmpty(emailAddress)) {
            emailAddress.error = "Email is required"
            if (isValid) {
                emailAddress.requestFocus()
                isValid = false
            }
        } else if (!isValidEmail(emailAddress)) {
            emailAddress.error = "Must be valid email. EXAMPLE@YOURDOMAIN.COM"
            if (isValid) {
                emailAddress.requestFocus()
                isValid = false
            }
        }
        if(!isEmpty(mobileNumber)) {
            if(!isValidMobile(mobileNumber)) {
                mobileNumber.error = "Must be valid phone number"
                if (isValid) {
                    mobileNumber.requestFocus()
                    isValid = false
                }
            }
        }
        return isValid
    }

    /**
     * Verify the signup form fields are not empty
     *
     * @param editText : EditText instance
     * @return Boolean
     */
    private fun isEmpty(editText: EditText): Boolean {
        val str: CharSequence = editText.text.toString()
        return TextUtils.isEmpty(str)
    }

    /**
     * Verify the password meets criteria
     * Password must be at least 8 character long with a combination of at least 1 uppercase, 1 lowercase and 1 number
     *
     * @param password: password string
     * @return Boolean
     */
    private fun isValidPassword(password: String): Boolean {
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    /**
     * Verify email using Patterns.EMAIL_ADDRESS
     *
     * @param text: email EditText instance
     * @return Boolean
     */
    private fun isValidEmail(text: EditText): Boolean {
        val email: CharSequence = text.text.toString()
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Verify valid mobile number using Patterns.PHONE
     *
     * @param phone: phone EditText instance
     * @return Boolean
     */
    private fun isValidMobile(phone: EditText): Boolean {
        val phoneNumber: CharSequence = phone.text.toString()
        return phoneNumber.length >= 10 && Patterns.PHONE.matcher(phoneNumber).matches()
    }

    /**
     * Perform signup
     *
     * @param cyberArkAccountBuilder: CyberArkAccountBuilder instance
     * @param signupData: signup form data
     */
    private fun performSignup(
        cyberArkAccountBuilder: CyberArkAccountBuilder,
        signupData: JSONObject,
        siteKey: String
    ) {
        val signupResponseHandler: LiveData<ResponseHandler<SignupCaptchaModel>> =
            CyberArkAuthProvider.signupWithCaptcha(cyberArkAccountBuilder)
                .start(this, signupData, siteKey)

        // Verify if there is any active observer, if not then add observer to get API response
        if (!signupResponseHandler.hasActiveObservers()) {
            signupResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        if (it.data!!.success) {
                            showSuccessPopup()
                        } else {
                            showErrorPopup(it.data!!.Message)
                        }
                    }
                    ResponseStatus.ERROR -> {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        showErrorPopup("Network Error: Unable to complete signup")
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
     * Show signup success popup
     *
     */
    private fun showSuccessPopup() {
        val intent = Intent(this, PopupActivity::class.java)
        intent.putExtra("title", getString(R.string.your_signup_is_successful))
        intent.putExtra("desc", getString(R.string.do_you_want_to_login))
        intent.putExtra("success", true)
        intent.putExtra("from_activity", "NativeSignupActivity")
        startActivity(intent)
        finish()
    }

    /**
     * Show signup error popup
     *
     * @param errorMessage: error message
     */
    private fun showErrorPopup(errorMessage: String) {
        val intent = Intent(this, PopupActivity::class.java)
        intent.putExtra("title", getString(R.string.your_signup_has_failed))
        intent.putExtra("desc", errorMessage)
        intent.putExtra("success", false)
        intent.putExtra("from_activity", "NativeSignupActivity")
        startActivity(intent)
        finish()
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