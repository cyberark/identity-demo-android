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

package com.cyberark.mfa.activity.common

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.mfa.R
import com.cyberark.mfa.activity.base.BaseActivity
import com.cyberark.mfa.activity.scenario1.MFAActivity
import com.cyberark.mfa.activity.scenario1.NativeSignupActivity
import com.cyberark.mfa.activity.scenario1.NativeSignupPopupActivity
import com.cyberark.mfa.activity.scenario2.NativeLoginActivity
import com.cyberark.mfa.utils.AppConfig

class LoginOptionsActivity : BaseActivity() {

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    private lateinit var account: CyberArkAccountBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_options)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        title = getString(R.string.acme)

        // Verify if access token is present or not
        val accessToken = KeyStoreProvider.get().getAuthToken()
        if (accessToken != null) {
            //Start MFA activity if access token is available
            val intent = Intent(this, MFAActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Invoke UI element
        progressBar = findViewById(R.id.progressBar_home_activity)

        // Setup CyberArk hosted login account
        account = AppConfig.setupAccountFromSharedPreference(this)
        // Setup native Login
        AppConfig.setupNativeLoginFromSharedPreference(this)
        // Setup Authentication Widget Login
        AppConfig.setupAuthWidgetFromSharedPreference(this)

        findViewById<CardView>(R.id.cv_redirect_login).setOnClickListener {
            val intent = Intent(this, NativeSignupPopupActivity::class.java)
            startForResult.launch(intent)
        }

        findViewById<CardView>(R.id.cv_mfa_widget_login).setOnClickListener {
            val intent = Intent(this, NativeLoginActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.authentication_widget).setOnClickListener {
            val intent = Intent(this, NativeLoginActivity::class.java)
            startActivity(intent)
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val alertData = result.data?.getStringExtra("ALERT_STATUS").toBoolean()
                if (alertData) {
                    when (result.data?.extras?.getInt("scenario")) {
                        1 -> {
                            when(result.data?.extras?.getInt("section")) {
                                1 -> {
                                    val intent = Intent(this, NativeSignupActivity::class.java)
                                    startActivity(intent)
                                }
                                2 -> {
                                    performCyberArkHostedLogin(account, progressBar)
                                }
                            }
                        }
                        2 -> {
                            val intent = Intent(this, NativeLoginActivity::class.java)
                            startActivity(intent)
                        }
                        3 -> {
                            val intent = Intent(this, NativeLoginActivity::class.java)
                            startActivity(intent)
                        }
                    }
                } else {
                    val popupData = result.data?.getStringExtra("POPUP_STATUS").toBoolean()
                    if (popupData) {
                        when (result.data?.extras?.getInt("section")) {
                            1 -> {
                                val intent = Intent(this, NativeSignupActivity::class.java)
                                startActivity(intent)
                            }
                            2 -> {
                                performCyberArkHostedLogin(account, progressBar)
                            }
                        }
                    }
                }
            }
        }

    fun showInfo(view: View) {
        when (view.id) {
            R.id.tv_redirect_login -> {
                val intent = Intent(this, AlertActivity::class.java)
                intent.putExtra("title", getString(R.string.signup_and_login_using_cyberArk_identity))
                intent.putExtra("desc", getString(R.string.signup_and_login_using_cyberArk_identity_desc))
                intent.putExtra("scenario", 1)
                startForResult.launch(intent)
            }
            R.id.tv_mfa_widget_login -> {
                val intent = Intent(this, AlertActivity::class.java)
                intent.putExtra("title", getString(R.string.mfa_widget_login_title))
                intent.putExtra("desc", getString(R.string.mfa_widget_login_description))
                intent.putExtra("scenario", 2)
                startForResult.launch(intent)
            }
            R.id.tv_authentication_widgets -> {
                val intent = Intent(this, AlertActivity::class.java)
                intent.putExtra("title", getString(R.string.authentication_widgets_title))
                intent.putExtra("desc", getString(R.string.authentication_widgets_description))
                intent.putExtra("scenario", 3)
                startForResult.launch(intent)
            }
        }
    }

    // **************** Handle menu settings click action Start *********************** //

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            //Start Settings activity
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("from_activity", "LoginOptionsActivity")
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    // **************** Handle menu settings click action End *********************** //
}