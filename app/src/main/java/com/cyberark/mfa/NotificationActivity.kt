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

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.NotificationDataModel
import com.cyberark.identity.data.model.OTPEnrollModel
import com.cyberark.identity.data.model.SubmitOTPModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.notification.NotificationConstants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.fcm.FCMReceiver
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Handle push Notification from activity
 *
 */
class NotificationActivity : AppCompatActivity() {

    companion object {
        private val TAG = NotificationActivity::class.simpleName
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var notificationDesc: TextView
    private lateinit var approveButton: Button
    private lateinit var denyButton: Button
    private lateinit var notificationData: NotificationDataModel
    private lateinit var otpEnrollModel: OTPEnrollModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        initializeData()
        invokeUI()
        updateUI()
    }

    /**
     * Initialize notification data
     *
     */
    private fun initializeData() {
        val notificationIntent = intent
        notificationData = notificationIntent.getParcelableExtra(FCMReceiver.NOTIFICATION_DATA)!!

        val otpEnrollData =
            CyberArkPreferenceUtil.getString(NotificationConstants.OTP_ENROLL_DATA, null)
        otpEnrollModel = Gson().fromJson(otpEnrollData, OTPEnrollModel::class.java)
    }

    /**
     * Invoke notification activity UI elements
     *
     */
    private fun invokeUI() {
        progressBar = findViewById(R.id.progressBar_notification_activity)
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

    /**
     * Update notification title and description
     *
     */
    private fun updateUI() {
        title = notificationData.AppName
        notificationDesc.setText(notificationData.Message)
    }

    /**
     * Call API to submit OTP code, notification challenge answer and user accepted status as true
     *
     */
    private fun approveNotification() {
        Log.i("NotificationActivity", "Approve")
        // Show progress indicator
        progressBar.visibility = View.VISIBLE
        val notificationPayload: JSONObject =
            getNotificationPayload(notificationData.ChallengeAnswer, true)
        submitOTP(this, otpEnrollModel, notificationPayload)
    }

    /**
     * Call API to submit OTP code, notification challenge answer and user accepted status as false
     *
     */
    private fun denyNotification() {
        Log.i("NotificationActivity", "Deny")
        // Show progress indicator
        progressBar.visibility = View.VISIBLE
        val notificationPayload: JSONObject =
            getNotificationPayload(notificationData.ChallengeAnswer, false)
        submitOTP(this, otpEnrollModel, notificationPayload)
    }

    /**
     * Submit OTP code to server
     *
     * @param context: application context
     * @param otpEnrollModel: OTPEnrollModel instance
     * @param notificationPayload: notification payload
     */
    private fun submitOTP(
        context: Context,
        otpEnrollModel: OTPEnrollModel,
        notificationPayload: JSONObject
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            val accessTokenData = KeyStoreProvider.get().getAuthToken()
            if (accessTokenData != null) {
                val submitOTPModel = CyberArkAuthProvider.submitOTP(setupFCMUrl(context))
                    .start(context, accessTokenData, otpEnrollModel, notificationPayload)
                handleSubmitOTPResponse(submitOTPModel)
            } else {
                Log.i(TAG, "Access Token is not initialized")
            }
            // Hide progress indicator
            progressBar.visibility = View.GONE
            finish()
        }
    }

    /**
     * Handle submit OTP response
     *
     * @param submitOTPModel: SubmitOTPModel instance
     */
    private fun handleSubmitOTPResponse(submitOTPModel: SubmitOTPModel) {
        if (submitOTPModel == null) {
            Log.i(TAG, "Submit OTP: Unable to get response from server")
        } else if (!submitOTPModel.success) {
            Log.i(TAG, submitOTPModel.Message)
        } else {
            Log.i(TAG, "User accepted the push notification")
        }
    }

    /**
     * Setup System URL and host URL in CyberArkAccountBuilder
     *
     * @return CyberArkAccountBuilder instance
     */
    private fun setupFCMUrl(context: Context): CyberArkAccountBuilder {
        return CyberArkAccountBuilder.Builder()
            .systemURL(context.getString(R.string.cyberark_account_system_url))
            .hostURL(context.getString(R.string.cyberark_account_host_url))
            .build()
    }

    /**
     * Get notification payload object
     *
     * @param challengeAnswer: notification challenge answer
     * @param userAccepted: user accepted status, true/false
     * @return JSONObject
     */
    private fun getNotificationPayload(challengeAnswer: String, userAccepted: Boolean): JSONObject {
        val notificationPayload = JSONObject()
        notificationPayload.put(NotificationConstants.OTP_CHALLENGE_ANSWER, challengeAnswer)
        notificationPayload.put(NotificationConstants.USER_ACCEPTED, userAccepted)
        return notificationPayload
    }

}