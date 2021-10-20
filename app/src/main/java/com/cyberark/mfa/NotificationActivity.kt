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

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.data.model.NotificationDataModel
import com.cyberark.mfa.fcm.FCMReceiver

class NotificationActivity : AppCompatActivity() {

    private lateinit var notificationDesc: TextView
    private lateinit var approveButton: Button
    private lateinit var denyButton: Button
    private lateinit var notificationData: NotificationDataModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        val notificationIntent = intent
        notificationData = notificationIntent.getParcelableExtra(FCMReceiver.NOTIFICATION_DATA)!!
        Log.i("NotificationActivity", notificationData.toString())
        Log.i("NotificationActivity", notificationData.AppName)
        invokeUI()
        updateUI()
    }

    private fun invokeUI() {
        notificationDesc = findViewById(R.id.notification_desc)
        approveButton = findViewById(R.id.approve_button)
        denyButton = findViewById(R.id.deny_button)

        approveButton.setOnClickListener {
            approveNotification()
        }
        denyButton.setOnClickListener {
            denyNotification()
        }
    }

    private fun updateUI() {
        title = notificationData.AppName
        notificationDesc.setText(notificationData.Message)
    }

    private fun approveNotification() {
        Log.i("NotificationActivity", "Approve")
    }

    private fun denyNotification() {
        Log.i("NotificationActivity", "Deny")
    }
}