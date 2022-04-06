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
import com.cyberark.mfa.activity.scenario1.MFAActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

/**
 * FCM token util class used to receive token from Firebase
 *
 */
class FCMTokenUtil {

    companion object {
        private val TAG = MFAActivity::class.simpleName
    }

    /**
     * Get FCM  token and update the response to FCMTokenInterface
     *
     */
    fun getFCMToken(fcmTokenInterface: FCMTokenInterface) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    fcmTokenInterface.onFcmTokenFailure(task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d(TAG, "Received token $token")
                if (token != null) {
                    fcmTokenInterface.onFcmTokenReceived(token)
                }
            })
    }
}