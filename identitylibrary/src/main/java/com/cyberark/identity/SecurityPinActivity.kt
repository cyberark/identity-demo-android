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

package com.cyberark.identity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

//TODO.. remove all hardcoded strings
class SecurityPinActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "SecurityPinActivity"
    private var authecticateBtn: Button? = null
    private var messageText: TextView? = null
    private var pinField: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(android.R.style.Theme_Dialog)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_pin)
        setUPUI()
    }

    @SuppressLint("ObsoleteSdkInt", "SetTextI18n")
    private fun setUPUI() {
        authecticateBtn = findViewById(R.id.authenticate)
        messageText = findViewById(R.id.messageText)
        pinField = findViewById(R.id.pinField)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            authecticateBtn?.setTextColor(resources.getColor(android.R.color.white, null))
            messageText?.setTextColor(resources.getColor(android.R.color.black, null))
        } else {
            authecticateBtn?.setTextColor(resources.getColor(android.R.color.white, null))
            messageText?.setTextColor(resources.getColor(android.R.color.black, null))
        }

        pinField?.hint = "Enter PIN here"
        messageText?.text = "Please enter PIN to authenticate"
        authecticateBtn?.setOnClickListener(this)
        authecticateBtn?.text = "Authenticate"
    }

    override fun onClick(view: View?) {
        if (authecticateBtn?.id != null && view?.id == authecticateBtn?.id) {
            //Authentiate tapped
            if (pinField!!.text.toString() == intent.getStringExtra("securitypin")) {
                setResult(RESULT_OK, Intent().putExtra("RESULT", "Success"))
                finish()
            } else {
                //Handle pin didn't match scenario
            }
        }
    }
}