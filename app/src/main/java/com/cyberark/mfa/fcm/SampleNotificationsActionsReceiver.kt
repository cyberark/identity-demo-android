package com.cyberark.mfa.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class SampleNotificationsActionsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action.equals(ACTION_APPROVE, ignoreCase = true)) {
            Log.i(TAG, "onReceive() ACTION_APPROVE :: " + ACTION_APPROVE)
        }
        if (intent.action != null && intent.action.equals(ACTION_DENY, ignoreCase = true)) {
            Log.i(TAG, "onReceive() ACTION_DENY :: " + ACTION_DENY)
        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID_SAMPLE_APP)
    }

    companion object {
        const val ACTION_APPROVE = "sample_action_approve"
        const val ACTION_DENY = "sample_action_deny"
        const val NOTIFICATION_ID_SAMPLE_APP = 5000
        const val NOTIFICATION_DATA = "NotificationData"
        const val BODY_CLICK = 0
        const val POSITIVE = 1
        const val NEGATIVE = 2
        private const val TAG = "NotificationReceiver"
    }
}