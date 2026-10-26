package com.strangerhelp.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.strangerhelp.app.service.NotificationChannels

data class ChannelStatus(
    val id: String,
    val name: String,
    val importance: Int,
    val exists: Boolean
)

data class NotificationSystemReport(
    val permissionsGranted: Boolean,
    val notificationsEnabled: Boolean,
    val channels: List<ChannelStatus>,
    val allChannelsConfigured: Boolean
)

object NotificationTester {

    const val CHANNEL_ID = NotificationChannels.TASKS

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun sendTestPushNotification(
        context: Context,
        title: String = "🔔 StrangerHelp Test Notification",
        body: String = "Your notification system is working! Push alerts & deep links are operational.",
        link: String = "/tasks",
        channelId: String = NotificationChannels.TASKS
    ): Boolean {
        return try {
            NotificationChannels.createAll(context)
            NotificationBuilder.show(
                context = context,
                title = title,
                body = body,
                link = link,
                channelId = channelId,
                notificationId = (System.currentTimeMillis() % 100000).toInt()
            )
            true
        } catch (e: Exception) {
            com.strangerhelp.app.utils.AppLogger.e("NotificationTester", "Failed to dispatch test notification", e)
            false
        }
    }

    fun runSystemDiagnostics(context: Context): NotificationSystemReport {
        NotificationChannels.createAll(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        val channelIds = listOf(
            NotificationChannels.DEFAULT,
            NotificationChannels.TASKS,
            NotificationChannels.MESSAGES,
            NotificationChannels.PROOF,
            NotificationChannels.PROMOTIONS
        )
        val channelStatuses = channelIds.map { id ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
                val ch = nm.getNotificationChannel(id)
                ChannelStatus(
                    id = id,
                    name = ch?.name?.toString() ?: id,
                    importance = ch?.importance ?: -1,
                    exists = ch != null
                )
            } else {
                ChannelStatus(id = id, name = id, importance = 3, exists = true)
            }
        }
        val perm = isNotificationPermissionGranted(context)
        val enabled = areNotificationsEnabled(context)
        return NotificationSystemReport(
            permissionsGranted = perm,
            notificationsEnabled = enabled,
            channels = channelStatuses,
            allChannelsConfigured = channelStatuses.all { it.exists }
        )
    }
}
