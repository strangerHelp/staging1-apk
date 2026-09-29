package com.strangerhelp.app.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.strangerhelp.app.MainActivity
import com.strangerhelp.app.navigation.DeepLinkHandler

object NotificationTapHandler {

    /**
     * Build a PendingIntent that opens MainActivity AND forwards the deep link.
     * The payload link may be:
     *   - /tasks/abc123
     *   - https://strangerhelp.com/tasks/abc123
     *   - /chat/conv1
     */
    fun buildPendingIntent(
        context: Context,
        link: String,
        notificationId: Int
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(
                if (link.startsWith("http")) link
                else "https://strangerhelp.com/" + link.trimStart('/')
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Extra fallback in case the URI isn't matched
            putExtra("deep_link", link)
        }
        return PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Called by FirebaseMessagingService or polling-based notification
     * manager when the user taps the notification.
     */
    fun onNotificationTapped(link: String) {
        DeepLinkHandler.handleLink(link)
    }
}
