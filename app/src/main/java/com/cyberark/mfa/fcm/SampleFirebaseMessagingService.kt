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

import android.os.Bundle
import android.util.Log
import com.cyberark.identity.builder.CyberArkAccountBuilder
import com.cyberark.identity.data.model.NotificationDataModel
import com.cyberark.identity.data.model.SendFCMTokenModel
import com.cyberark.identity.provider.CyberArkAuthProvider
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.utils.AppUtils
import com.cyberark.mfa.utils.PreferenceConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class SampleFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = SampleFirebaseMessagingService::class.simpleName
    }

    val DATA_KEY = "data_key" // this is the key sent from Cloud

    val DATA_VALUE = "data_value" // this is the value for the key

    val NOTIFY_TIME = "notify_time" // this is the time when the cloud sends the push.


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //TODO.. implement push notification util for handling message and acknowledgement
        Log.i("FCM 1", remoteMessage.data.toString())
        Log.i("FCM 2", remoteMessage.toString())
        Log.i("FCM 3", remoteMessage.notification.toString())
        val data1: Map<String, String> = remoteMessage.data
        Log.i("FCM 5", data1.toString())
        Log.i("FCM 6", remoteMessage.notification?.body.toString())

        /**
         * if application is in foreground process the push in activity
         */
        if (AppUtils.isAppOnForeground()) {
            Log.i(TAG, "App in foreground from FCM AppUtils ")
        } else {
            /**
             * if the application is in the background build and show Notification
             */
            Log.i(TAG, "App in background from FCM AppUtils ")
        }

        val data: Map<String, String> = remoteMessage.data
        val bundle = Bundle()
        bundle.putString(DATA_KEY, data[DATA_KEY])
        bundle.putString(DATA_VALUE, data[DATA_VALUE])
        bundle.putString(NOTIFY_TIME, data[NOTIFY_TIME])
        handleMessage(bundle)
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
     * @param sendFCMTokenModel: SendFCMTokenModel model class instance
     */
    private fun handleUploadFCMTokenResponse(sendFCMTokenModel: SendFCMTokenModel?) {
        if (sendFCMTokenModel == null) {
            Log.i(TAG, "Unable to get response from server")
        } else if (!sendFCMTokenModel.Status) {
            Log.i(TAG, "Unable to upload FCM Token to Server")
        } else {
            Log.i(TAG, "Uploaded FCM Token to Server successfully")
        }
    }

    /**
     * Setup System URL and host URL in CyberArkAccountBuilder to upload FCM token
     *
     * @return CyberArkAccountBuilder instance
     */
    private fun setupFCMUrl(): CyberArkAccountBuilder {
        return CyberArkAccountBuilder.Builder()
            .systemURL(getString(R.string.cyberark_account_system_url))
            .hostURL(getString(R.string.cyberark_account_host_url))
            .build()
    }

    fun handleMessage(urlEncodedBundle: Bundle) {
        val bundle: Bundle = urlDecodeDataValue(urlEncodedBundle)

        val key = bundle.getString(DATA_KEY)
        val value = bundle.getString(DATA_VALUE)
        val notifyTime = bundle.getString(NOTIFY_TIME)

        Log.i(TAG, "KEY :: " + key)
        Log.i(TAG, "value :: " + value)
        Log.i(TAG, "KEY :: " + notifyTime)


        val notificationDataModel = Gson().fromJson<NotificationDataModel>(value.toString(), NotificationDataModel::class.java)
        Log.i(TAG, "notificationDataModel :: " + notificationDataModel.AppIconUrl)
        Log.i(TAG, "notificationDataModel :: " + notificationDataModel.AppName)
        Log.i(TAG, "notificationDataModel :: " + notificationDataModel.ChallengeAnswer)
        Log.i(TAG, "notificationDataModel :: " + notificationDataModel.CollapseId)
        Log.i(TAG, "notificationDataModel :: " + notificationDataModel.CommandUuid)

        var sampleNotificationsManager = SampleNotificationsManager(this)
        sampleNotificationsManager.sendNotification(notificationDataModel)

    }

    private fun urlDecodeDataValue(input: Bundle): Bundle {
        val out = Bundle(input)
        var value = out.getString(DATA_VALUE)
        if (value != null) {
            try {
                value = URLDecoder.decode(value, "UTF-8")
                out.putString(DATA_VALUE, value)
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "UrlDecodeDataValue Failed: ", e)
            }
        }
        return out
    }

   /* *//**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     *//*
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.fcm_message))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build())
    }*/
}