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

class SampleNotificationsManager(private val context: Context) {

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
   fun sendNotification(notificationDataModel: NotificationDataModel) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channelId = context.getString(R.string.notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notificationDataModel.Title)
            .setContentText(notificationDataModel.Message)
            .addAction(createDenyAction())
            .addAction(createApproveAction())
            .setAutoCancel(true)
            .setContentIntent(createOnTapPendingIntent())
            .setSound(defaultSoundUri)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = context.getString(R.string.notification_channel_description)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(SampleNotificationsActionsReceiver.NOTIFICATION_ID_SAMPLE_APP, notificationBuilder.build())
    }

    /*
     * create an action that sends approve intent to the broadcast receiver
     */
    private fun createApproveAction(): NotificationCompat.Action {
        val approveIntent = Intent(context, SampleNotificationsActionsReceiver::class.java)
        approveIntent.action = SampleNotificationsActionsReceiver.ACTION_APPROVE
        /*
         * Very important to set request code and flag, so the PendingIntent
         * will be unique within the system and the bundle containing data will not be lost
         */
        val approvePendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            SampleNotificationsActionsReceiver.POSITIVE,
            approveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.notification_action_approve),
            approvePendingIntent
        ).build()
    }

    /*
     * create an action that sends deny intent to the broadcast receiver
     */
    private fun createDenyAction(): NotificationCompat.Action {
        val denyIntent = Intent(context, SampleNotificationsActionsReceiver::class.java)
        denyIntent.action = SampleNotificationsActionsReceiver.ACTION_DENY
        /*
         * Very important to set request code and flag, so the PendingIntent
         * will be unique within the system and the bundle containing data will not be lost
         */
        val denyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            SampleNotificationsActionsReceiver.NEGATIVE,
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
        return PendingIntent.getActivity(context,  (System.currentTimeMillis() and 0xfffffff).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }
}