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

package com.cyberark.mfa.scenario2

import android.content.Intent
import android.os.Bundle
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
import com.cyberark.identity.builder.CyberArkWidgetBuilder
import com.cyberark.identity.data.model.BasicLoginModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.*
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.mfa.R
import com.cyberark.mfa.utils.AppConfig

class NativeLoginActivity : AppCompatActivity() {

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

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
        progressBar = findViewById(R.id.progressBar_home_activity)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)

        val loginButton = findViewById<Button>(R.id.button_login)
        loginButton.setOnClickListener {
            hideKeyboard(loginButton)
            if (username.text.isBlank() || password.text.isBlank()) {
                showLoginErrorAlert()
            } else {
                basicLogin(username.text.toString(), password.text.toString())
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
    private fun basicLogin(username: String, password: String) {
        val account: CyberArkWidgetBuilder = AppConfig.setupNativeLoginFromSharedPreference(this)
        val authResponseHandler: LiveData<ResponseHandler<BasicLoginModel>> =
            CyberArkAuthProvider.nativeLogin(account)
                .start(this, username, password)
        // Verify if there is any active observer, if not then add observer to get API response
        if (!authResponseHandler.hasActiveObservers()) {
            authResponseHandler.observe(this, {
                when (it.status) {
                    ResponseStatus.SUCCESS -> {
                        // Save session token
                        KeyStoreProvider.get().saveSessionToken(it.data!!.Result.SessionUuid)
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Start TransferFundActivity
                        val intent = Intent(this, TransferFundActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    ResponseStatus.ERROR -> {
                        // Hide progress indicator
                        progressBar.visibility = View.GONE
                        // Show login error message using Toast
                        showLoginErrorAlert()
                    }
                    ResponseStatus.LOADING -> {
                        // Show progress indicator
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
        }
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
                }
            }
        })
        enrollFingerPrintDlg.displayAlert(
            this,
            this.getString(R.string.dialog_login_error_header_text),
            this.getString(R.string.dialog_login_error_desc), true,
            mutableListOf(
                AlertButton("OK", AlertButtonType.POSITIVE)
            )
        )
    }
}