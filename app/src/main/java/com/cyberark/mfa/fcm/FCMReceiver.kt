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
import com.cyberark.identity.data.model.NotificationDataModel

class FCMReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = FCMReceiver::class.simpleName
        const val ACTION_APPROVE = "sample_action_approve"
        const val ACTION_DENY = "sample_action_deny"
        const val NOTIFICATION_DATA = "NotificationData"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationData = intent.getParcelableExtra<NotificationDataModel>(NOTIFICATION_DATA)
        when {
            intent.action != null && intent.action.equals(ACTION_APPROVE, ignoreCase = true) -> {
                Log.i(TAG, ACTION_APPROVE)
            }
            intent.action != null && intent.action.equals(ACTION_DENY, ignoreCase = true) -> {
                Log.i(TAG, ACTION_DENY)
            }
        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationData?.CommandUuid.hashCode())
    }
}