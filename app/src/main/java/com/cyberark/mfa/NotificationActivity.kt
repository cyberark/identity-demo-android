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