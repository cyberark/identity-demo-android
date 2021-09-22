package com.cyberark.mfa.fcm

import android.util.Log
import com.cyberark.identity.util.keystore.KeyStoreProvider
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.util.PreferenceConstants
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