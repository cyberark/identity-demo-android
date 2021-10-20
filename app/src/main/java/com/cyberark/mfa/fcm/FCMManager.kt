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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cyberark.identity.data.model.NotificationDataModel
import com.cyberark.mfa.HomeActivity
import com.cyberark.mfa.R

class FCMManager(private val context: Context) {

    companion object {
        private val TAG = FCMManager::class.simpleName
        private const val POSITIVE = 1
        private const val NEGATIVE = 2
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param notificationDataModel FCM message body received.
     */
    fun sendNotification(notificationDataModel: NotificationDataModel) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channelId = context.getString(R.string.notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_identity_foreground)
            .setContentTitle(notificationDataModel.Title)
            .setContentText(notificationDataModel.Message)
            .addAction(createDenyAction(notificationDataModel))
            .addAction(createApproveAction(notificationDataModel))
            .setAutoCancel(true)
            .setContentIntent(createOnTapPendingIntent())
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationDataModel.Message))

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = context.getString(R.string.notification_channel_description)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(
            notificationDataModel.CommandUuid.hashCode(),
            notificationBuilder.build()
        )
    }

    /*
     * create an action that sends approve intent to the broadcast receiver
     */
    private fun createApproveAction(notificationDataModel: NotificationDataModel): NotificationCompat.Action {
        val approveIntent = Intent(context, FCMReceiver::class.java)
        approveIntent.action = FCMReceiver.ACTION_APPROVE
        approveIntent.putExtra(FCMReceiver.NOTIFICATION_DATA, notificationDataModel)
        /*
         * Very important to set request code and flag, so the PendingIntent
         * will be unique within the system and the bundle containing data will not be lost
         */
        val approvePendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            POSITIVE,
            approveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Action.Builder(
            android.R.drawable.btn_star,
            context.getString(R.string.notification_action_approve),
            approvePendingIntent
        ).build()
    }

    /*
     * create an action that sends deny intent to the broadcast receiver
     */
    private fun createDenyAction(notificationDataModel: NotificationDataModel): NotificationCompat.Action {
        val denyIntent = Intent(context, FCMReceiver::class.java)
        denyIntent.action = FCMReceiver.ACTION_DENY
        denyIntent.putExtra(FCMReceiver.NOTIFICATION_DATA, notificationDataModel)
        /*
         * Very important to set request code and flag, so the PendingIntent
         * will be unique within the system and the bundle containing data will not be lost
         */
        val denyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            NEGATIVE,
            denyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.notification_action_deny),
            denyPendingIntent
        ).build()
    }

    private fun createOnTapPendingIntent(): PendingIntent {
        val intent = Intent(context, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return PendingIntent.getActivity(
            context, (System.currentTimeMillis() and 0xfffffff).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}