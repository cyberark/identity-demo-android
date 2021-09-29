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

import android.util.Log
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.utils.PreferenceConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SampleFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = SampleFirebaseMessagingService::class.simpleName
    }

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
        //TODO.. implement API call to send token to server
    }
}