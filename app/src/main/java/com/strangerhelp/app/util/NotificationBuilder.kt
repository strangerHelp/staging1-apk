package com.strangerhelp.app.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.strangerhelp.app.MainActivity
import com.strangerhelp.app.R
import com.strangerhelp.app.service.NotificationChannels

/**
 * Builds and displays a tappable notification that opens the correct screen.
 */
object NotificationBuilder {

    fun normalizeDeepLinkUrl(link: String): String {
        return when {
            link.startsWith("http") -> link
            link.startsWith("/")    -> "https://strangerhelp.com$link"
            else                    -> "https://strangerhelp.com/$link"
        }
    }

    fun buildNotification(
        context: Context,
        title: String,
        body: String,
        link: String,
        channelId: String,
        notificationId: Int
    ): android.app.Notification {
        NotificationChannels.createAll(context)

        val deepLinkUrl = normalizeDeepLinkUrl(link)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(deepLinkUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("deep_link", link)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (channelId == NotificationChannels.TASKS ||
                    channelId == NotificationChannels.MESSAGES ||
                    channelId == NotificationChannels.PROOF) {
                    NotificationCompat.PRIORITY_HIGH
                } else NotificationCompat.PRIORITY_DEFAULT
            )
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
    }

    fun show(
        context: Context,
        title: String,
        body: String,
        link: String,
        channelId: String,
        notificationId: Int
    ) {
        val notification = buildNotification(
            context = context,
            title = title,
            body = body,
            link = link,
            channelId = channelId,
            notificationId = notificationId
        )

        try {
            NotificationManagerCompat.from(context)
                .notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted on Android 13+
            com.strangerhelp.app.utils.AppLogger.w("NotificationBuilder", "POST_NOTIFICATIONS permission not granted", e)
        } catch (e: Exception) {
            com.strangerhelp.app.utils.AppLogger.e("NotificationBuilder", "Error dispatching notification", e)
        }
    }
}
