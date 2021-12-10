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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.mfa.utils.AppConfig

class LoginOptionsActivity : BaseActivity() {

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    private lateinit var account: CyberArkAccountBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_options)

        // Verify if access token is present or not
        val accessToken = KeyStoreProvider.get().getAuthToken()
        if (accessToken != null) {
            //Start MFA activity if access token is available
            val intent = Intent(this, MFAActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Invoke UI element
        progressBar = findViewById(R.id.progressBar_home_activity)

        // Setup account
        account = AppConfig.setupAccountFromSharedPreference(this)

        findViewById<CardView>(R.id.cv_redirect_login).setOnClickListener {
            login(account, progressBar)
        }
        inflateMenuFromToolbar()
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra("ALERT_LOGIN_STATUS").toBoolean()
                if (data) {
                    login(account, progressBar)
                }
            }
        }

    fun showInfo(view: View) {
        val intent = Intent(this, AlertActivity::class.java)
        intent.putExtra("title", getString(R.string.login_hosted_title))
        intent.putExtra("desc", getString(R.string.login_hosted_description))
        startForResult.launch(intent)
    }

    // **************** Handle menu settings click action Start *********************** //

    private fun inflateMenuFromToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.title = "Acme"
        }
        toolbar.inflateMenu(R.menu.settings_menu)
    }

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