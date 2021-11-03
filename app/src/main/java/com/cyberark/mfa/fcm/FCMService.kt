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

import android.content.Intent
import android.util.Log
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.NotificationDataModel
import com.cyberark.identity.data.model.SendFCMTokenModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.NotificationActivity
import com.cyberark.mfa.R
import com.cyberark.mfa.utils.AppUtils
import com.cyberark.mfa.utils.PreferenceConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class FCMService : FirebaseMessagingService() {

    companion object {
        private val TAG = FCMService::class.simpleName
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            if (remoteMessage.data.isNotEmpty()) {
                val notificationDataModel: NotificationDataModel =
                    CyberArkAuthProvider.parseRemoteNotification(remoteMessage.data).start()

                /**
                 * if application is in foreground process the notification in activity
                 */
                if (AppUtils.isAppOnForeground()) {
                    Log.i(TAG, "FCMService() App in foreground")
                    val intent = Intent(this, NotificationActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(FCMReceiver.NOTIFICATION_DATA, notificationDataModel)
                    startActivity(intent)
                } else {
                    /**
                     * if the application is in the background build and show Notification
                     */
                    Log.i(TAG, "FCMService() App in background")
                    FCMManager(this).sendNotification(notificationDataModel)
                }
            } else {
                Log.i(TAG, "FCMService() remote message contains empty data payload")
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "FCMService() This is not a CyberArk Notification", e)
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        if (KeyStoreProvider.get().getAuthToken() == null) {
            Log.i(TAG, "Got FCM token but not yet authenticated")
            return
        }
        if (!CyberArkPreferenceUtil.getBoolean(PreferenceConstants.ENROLLMENT_STATUS, false)) {
            Log.i(TAG, "Got FCM token but not yet enrolled")
            return
        }
        uploadFCMTokenToCyberArkServer(token)
    }

    /**
     * Upload FCM token to CyberArk server
     *
     * @param token: FCM token
     */
    private fun uploadFCMTokenToCyberArkServer(token: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val accessTokenData = KeyStoreProvider.get().getAuthToken()
            if (accessTokenData != null) {
                val sendFCMTokenModel: SendFCMTokenModel? =
                    CyberArkAuthProvider.sendFCMToken(setupFCMUrl())
                        .start(applicationContext, token, accessTokenData)
                handleUploadFCMTokenResponse(sendFCMTokenModel)
            } else {
                Log.i(TAG, "Access Token is not initialized")
            }
        }
    }

    /**
     * Handle upload FCM token response
     *
     * @param sendFCMTokenModel: SendFCMTokenModel instance
     */
    private fun handleUploadFCMTokenResponse(sendFCMTokenModel: SendFCMTokenModel?) {
        if (sendFCMTokenModel == null) {
            Log.i(TAG, "Upload FCM Token: Unable to get response from server")
        } else if (!sendFCMTokenModel.Status) {
            Log.i(TAG, "Unable to upload FCM Token to Server")
        } else {
            Log.i(TAG, "Uploaded FCM Token to Server successfully")
        }
    }

    /**
     * Setup System URL and host URL in CyberArkAccountBuilder
     *
     * @return CyberArkAccountBuilder instance
     */
    private fun setupFCMUrl(): CyberArkAccountBuilder {
        return CyberArkAccountBuilder.Builder()
            .systemURL(getString(R.string.cyberark_account_system_url))
            .hostURL(getString(R.string.cyberark_account_host_url))
            .build()
    }
}