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

package com.cyberark.mfa.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.mfa.R
import com.cyberark.mfa.activity.base.BaseActivity
import com.cyberark.mfa.scenario1.NativeSignupActivity
import com.cyberark.mfa.utils.AppConfig

class PopupActivity : BaseActivity() {

    // Progress indicator variable
    private lateinit var progressBar: ProgressBar

    private lateinit var account: CyberArkAccountBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_popup)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        updateUI()
    }

    /**
     * Update UI components
     *
     */
    private fun updateUI() {
        // Invoke UI element
        progressBar = findViewById(R.id.progressBar_popup_activity)

        // Setup CyberArk hosted login account
        account = AppConfig.setupAccountFromSharedPreference(this)

        val confirmButton: Button = findViewById(R.id.button_confirm)
        val iconSuccessLayout = findViewById<FrameLayout>(R.id.success_layout)
        val iconFailureLayout = findViewById<FrameLayout>(R.id.failure_layout)

        val cancelDialog = findViewById<LinearLayout>(R.id.cancel_dialog)
        cancelDialog.setOnClickListener {
            finish()
        }

        val activityIntent = intent
        when {
            activityIntent.getStringExtra("from_activity").equals("NativeSignupActivity") -> {
                if(activityIntent.extras?.getBoolean("success") == true) {
                    iconSuccessLayout.visibility = View.VISIBLE
                    iconFailureLayout.visibility = View.GONE
                    confirmButton.text = getString(R.string.login)
                    confirmButton.setOnClickListener {
                        login(account, progressBar)
                        finish()
                    }
                } else {
                    iconSuccessLayout.visibility = View.GONE
                    iconFailureLayout.visibility = View.VISIBLE
                    confirmButton.text = getString(R.string.button_retry)
                    confirmButton.setOnClickListener {
                        val intent = Intent(this, NativeSignupActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            else -> {
                confirmButton.setOnClickListener {
                    finish()
                }
            }
        }
        val headerText: TextView? = findViewById(R.id.header_text)
        headerText?.text = activityIntent.extras?.getString("title")
        val contentText: TextView? = findViewById(R.id.content_text)
        contentText?.text = activityIntent.extras?.getString("desc")
    }
}