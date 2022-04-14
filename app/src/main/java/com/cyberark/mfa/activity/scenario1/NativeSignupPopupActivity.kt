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

package com.cyberark.mfa.activity.scenario1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.mfa.R

class NativeSignupPopupActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_native_signup_popup)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        invokeUI()
    }

    private fun invokeUI() {
        val buttonSignup = findViewById<Button>(R.id.button_signup)
        buttonSignup.setOnClickListener(this)
        val buttonLogin = findViewById<Button>(R.id.button_login)
        buttonLogin.setOnClickListener(this)
        val cancelDialog = findViewById<LinearLayout>(R.id.cancel_dialog)
        cancelDialog.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_signup -> {
                val intent = Intent()
                intent.putExtra("POPUP_STATUS", "true")
                intent.putExtra("section", 1)
                setResult(RESULT_OK, intent)
                finish()
            }
            R.id.button_login -> {
                val intent = Intent()
                intent.putExtra("POPUP_STATUS", "true")
                intent.putExtra("section", 2)
                setResult(RESULT_OK, intent)
                finish()
            }
            R.id.cancel_dialog -> {
                val intent = Intent()
                intent.putExtra("POPUP_STATUS", "false")
                setResult(RESULT_CANCELED, intent)
                finish()
            }
        }
    }
}