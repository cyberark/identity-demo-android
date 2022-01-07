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
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.mfa.R
import com.cyberark.mfa.scenario1.MFAActivity
import com.cyberark.mfa.scenario2.TransferFundActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Verify if access token is present or not
        val accessToken = KeyStoreProvider.get().getAuthToken()
        if (accessToken != null) {
            //Start MFA activity if access token is available
            val intent = Intent(this, MFAActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Verify if session token is present or not
        val sessionToken = KeyStoreProvider.get().getSessionToken()
        if (sessionToken != null) {
            //Start TransferFundActivity if session token is available
            val intent = Intent(this, TransferFundActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupHyperlink()

        val start: Button = findViewById(R.id.button_start)
        start.setOnClickListener {
            //Start MFA activity if access token is available
            val intent = Intent(this, LoginOptionsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupHyperlink() {
        val linkTextView: TextView = findViewById(R.id.api_doc)
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}