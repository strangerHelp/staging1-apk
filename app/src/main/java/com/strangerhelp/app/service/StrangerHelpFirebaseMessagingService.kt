package com.strangerhelp.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.strangerhelp.app.MainActivity
import com.strangerhelp.app.R

class StrangerHelpFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "StrangerHelp"
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val link = data["link"] ?: ""

        showNotification(title, body, link)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token logic could go here to sync with backend
    }

    private fun showNotification(title: String, body: String, link: String) {
        val notificationId = System.currentTimeMillis().toInt()
        val pendingIntent = NotificationTapHandler.buildPendingIntent(
            context = this,
            link = link.ifBlank { "/tasks" },
            notificationId = notificationId
        )

        val channelId = "strangerhelp_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "StrangerHelp Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
