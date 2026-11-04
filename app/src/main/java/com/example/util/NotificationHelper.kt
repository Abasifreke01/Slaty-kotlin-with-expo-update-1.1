package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_ID = "slaty_messages_channel"
    const val CHANNEL_NAME = "New Messages & Academic Updates"
    private var notificationIdCounter = 2001

    // Intent Extra Keys
    const val EXTRA_FROM_FCM = "extra_from_fcm"
    const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
    const val EXTRA_NOTIFICATION_BODY = "extra_notification_body"
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
    const val EXTRA_SENDER_NAME = "extra_sender_name"
    const val EXTRA_CHAT_ID = "extra_chat_id"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Real-time push notifications for student and tutor direct messages, community updates, and task alerts"
                enableVibration(true)
                setShowBadge(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a rich notification for both foreground FCM deliveries and background data messages
     */
    fun showPushNotification(
        context: Context,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        senderName: String = "",
        isCommunity: Boolean = false
    ) {
        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val type = data["type"] ?: if (isCommunity) "community" else "message"
        val chatId = data["chatId"] ?: data["channelId"] ?: ""
        val resolvedSender = senderName.ifBlank { data["senderName"] ?: "Slaty Tutor" }

        // Intent to launch MainActivity when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_FROM_FCM, true)
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_BODY, body)
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
            putExtra(EXTRA_SENDER_NAME, resolvedSender)
            putExtra(EXTRA_CHAT_ID, chatId)
            // Forward any custom data keys
            data.forEach { (k, v) -> putExtra("fcm_data_$k", v) }
        }

        val reqCode = (title.hashCode() + body.hashCode()).and(0x7FFFFFFF)
        val pendingIntent = PendingIntent.getActivity(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_slaty_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)

        try {
            val nid = notificationIdCounter++
            NotificationManagerCompat.from(context).notify(nid, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showNewMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        isCommunity: Boolean = false
    ) {
        val title = if (isCommunity) "$senderName in Study Hub" else "New message from $senderName"
        showPushNotification(
            context = context,
            title = title,
            body = messageText,
            data = mapOf(
                "senderName" to senderName,
                "type" to if (isCommunity) "community" else "message"
            ),
            senderName = senderName,
            isCommunity = isCommunity
        )
    }
}
