package com.example.service

import android.content.Context
import android.util.Log
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles Firebase Cloud Messaging (FCM) push notifications.
 *
 * - When in FOREGROUND: onMessageReceived is triggered and posts an immediate heads-up
 *   notification so students and tutors are alerted right away.
 * - When in BACKGROUND:
 *     - Data messages: onMessageReceived is invoked directly by the OS to parse payload and display notification.
 *     - Notification messages: Google Play Services displays the notification; on tapping, payload is passed to MainActivity.
 */
class SlatyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "SlatyFCM"
        const val PREFS_NAME = "slaty_fcm_prefs"
        const val KEY_FCM_TOKEN = "fcm_registration_token"

        fun getSavedFcmToken(context: Context): String? {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_FCM_TOKEN, null)
        }

        fun saveFcmToken(context: Context, token: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FCM_TOKEN, token)
                .apply()
        }
    }

    @Deprecated("Overrides deprecated member in FirebaseMessagingService")
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "Refreshed FCM registration token: $token")
        saveFcmToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        // 1. Ensure notification channel exists
        NotificationHelper.createNotificationChannel(applicationContext)

        // 2. Extract notification & data fields
        val notification = remoteMessage.notification
        val data = remoteMessage.data

        val title = notification?.title
            ?: data["title"]
            ?: data["senderName"]?.let { "New message from $it" }
            ?: "New Academic Message"

        val body = notification?.body
            ?: data["body"]
            ?: data["message"]
            ?: data["text"]
            ?: "You have a new update in Slaty."

        val senderName = data["senderName"] ?: ""
        val isCommunity = data["isCommunity"]?.toBooleanStrictOrNull() ?: false

        Log.d(TAG, "Showing push notification: '$title' - '$body'")

        // 3. Post notification to notification manager
        NotificationHelper.showPushNotification(
            context = applicationContext,
            title = title,
            body = body,
            data = data,
            senderName = senderName,
            isCommunity = isCommunity
        )
    }
}
