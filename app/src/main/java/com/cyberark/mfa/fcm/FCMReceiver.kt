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

package com.cyberark.mfa.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.NotificationDataModel
import com.cyberark.identity.data.model.OTPEnrollModel
import com.cyberark.identity.data.model.SubmitOTPModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.notification.NotificationConstants
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class FCMReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = FCMReceiver::class.simpleName
        const val ACTION_APPROVE = "fcm_action_approve"
        const val ACTION_DENY = "fcm_action_deny"
        const val NOTIFICATION_DATA = "NotificationData"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationData = intent.getParcelableExtra<NotificationDataModel>(NOTIFICATION_DATA)
        val otpEnrollData =
            CyberArkPreferenceUtil.getString(NotificationConstants.OTP_ENROLL_DATA, null)
        val otpEnrollModel = Gson().fromJson(otpEnrollData, OTPEnrollModel::class.java)
        when {
            intent.action != null && intent.action.equals(ACTION_APPROVE, ignoreCase = true) -> {
                Log.i(TAG, ACTION_APPROVE)
                val notificationPayload: JSONObject =
                    getNotificationPayload(notificationData!!.ChallengeAnswer, true)
                submitOTP(context, otpEnrollModel, notificationPayload)
            }
            intent.action != null && intent.action.equals(ACTION_DENY, ignoreCase = true) -> {
                Log.i(TAG, ACTION_DENY)
                val notificationPayload: JSONObject =
                    getNotificationPayload(notificationData!!.ChallengeAnswer, false)
                submitOTP(context, otpEnrollModel, notificationPayload)
            }
        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationData?.CommandUuid.hashCode())
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
        GlobalScope.launch(Dispatchers.Main) {
            val accessTokenData = KeyStoreProvider.get().getAuthToken()
            if (accessTokenData != null) {
                val submitOTPModel = CyberArkAuthProvider.submitOTP(setupFCMUrl(context))
                    .start(context, accessTokenData, otpEnrollModel, notificationPayload)
                handleSubmitOTPResponse(submitOTPModel)
            } else {
                Log.i(TAG, "Access Token is not initialized")
            }
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