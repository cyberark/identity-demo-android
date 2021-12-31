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

package com.cyberark.mfa.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.mfa.R

class AlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_alert)

        val headerText: TextView? = findViewById(R.id.header_text)
        headerText?.text = intent.extras?.getString("title")
        val contentText: TextView? = findViewById(R.id.content_text)
        contentText?.text = intent.extras?.getString("desc")

        val cancelButton: Button = findViewById(R.id.button_cancel)
        cancelButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("ALERT_LOGIN_STATUS", "false")
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        val loginButton: Button = findViewById(R.id.button_login)
        loginButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("ALERT_LOGIN_STATUS", "true")
            setResult(RESULT_OK, intent)
            finish()
        }
        setupHyperlink()
    }

    private fun setupHyperlink() {
        val linkTextView: TextView = findViewById(R.id.end_text)
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}