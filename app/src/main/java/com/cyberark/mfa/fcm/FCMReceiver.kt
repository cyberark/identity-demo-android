package com.cyberark.mfa.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.cyberark.identity.data.model.NotificationDataModel

class FCMReceiver : BroadcastReceiver() {

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

    companion object {
        private const val TAG = "NotificationReceiver"
        const val ACTION_APPROVE = "sample_action_approve"
        const val ACTION_DENY = "sample_action_deny"
        const val NOTIFICATION_DATA = "NotificationData"
    }
}