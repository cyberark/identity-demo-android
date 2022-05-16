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

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.mfa.R

class AlertActivity : AppCompatActivity(), View.OnClickListener {

    private var scenarioNo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_alert)

        val headerText: TextView? = findViewById(R.id.header_text)
        headerText?.text = intent.extras?.getString("title")
        val contentText: TextView? = findViewById(R.id.content_text)
        contentText?.text = intent.extras?.getString("desc")
        scenarioNo = intent.extras?.getInt("scenario")!!

        invokeUI()
        setupHyperlink()
    }

    private fun invokeUI() {
        val buttonSignup = findViewById<Button>(R.id.button_signup)
        buttonSignup.setOnClickListener(this)
        val buttonLogin = findViewById<Button>(R.id.button_login)
        buttonLogin.setOnClickListener(this)
        val cancelDialog = findViewById<LinearLayout>(R.id.cancel_dialog)
        cancelDialog.setOnClickListener(this)

        if(scenarioNo == 2) {
            buttonSignup.visibility = View.GONE
        }
        if(scenarioNo == 3) {
            buttonSignup.visibility = View.GONE
            val headerText: TextView? = findViewById(R.id.end_text)
            headerText?.visibility = View.GONE
            buttonLogin.text = getString(R.string.authentication_widgets_proceed)
        }
    }

    private fun setupHyperlink() {
        val linkTextView: TextView = findViewById(R.id.end_text)
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.button_signup -> {
                val intent = Intent()
                intent.putExtra("ALERT_STATUS", "true")
                intent.putExtra("scenario", scenarioNo)
                intent.putExtra("section", 1)
                setResult(RESULT_OK, intent)
                finish()
            }
            R.id.button_login -> {
                when (scenarioNo) {
                    1,2 -> {
                        val intent = Intent()
                        intent.putExtra("ALERT_STATUS", "true")
                        intent.putExtra("scenario", scenarioNo)
                        intent.putExtra("section", 2)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                    3 -> {
                        val intent = Intent()
                        intent.putExtra("ALERT_STATUS", "true")
                        intent.putExtra("scenario", scenarioNo)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
            R.id.cancel_dialog -> {
                val intent = Intent()
                intent.putExtra("ALERT_STATUS", "false")
                setResult(RESULT_CANCELED, intent)
                finish()
            }
        }
    }
}