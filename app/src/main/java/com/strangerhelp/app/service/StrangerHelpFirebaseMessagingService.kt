package com.strangerhelp.app.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.strangerhelp.app.util.NotificationBuilder

/**
 * Receives FCM data messages and displays a local notification.
 *
 * The backend sends DATA messages (not notification messages) so we can
 * control the channel, icon and deep-link per event type.
 */
class StrangerHelpFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
        // POST /api/push/fcm { token, deviceId } to backend
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        if (data.isEmpty() && remoteMessage.notification == null) {
            Log.w(TAG, "Empty data payload; ignoring.")
            return
        }

        val title = data["title"] ?: remoteMessage.notification?.title ?: "StrangerHelp"
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val link = data["link"] ?: ""          // e.g. /tasks/abc123
        val type = data["type"] ?: "general"   // task_claimed, message, proof, etc.

        val channel = NotificationChannels.resolve(type)

        NotificationBuilder.show(
            context = this,
            title = title,
            body = body,
            link = link,
            channelId = channel,
            notificationId = System.currentTimeMillis().toInt()
        )
    }

    companion object { private const val TAG = "FCMService" }
}
