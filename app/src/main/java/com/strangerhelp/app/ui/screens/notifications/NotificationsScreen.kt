package com.strangerhelp.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.Notification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Start polling when screen is visible
    LaunchedEffect(Unit) {
        viewModel.startPolling()
    }

    // Stop polling when screen is removed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Text("Mark all read", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                com.strangerhelp.app.ui.components.EmptyState(
                    icon = "🔔",
                    title = "No notifications yet",
                    message = "We'll notify you when something happens"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            // Navigate based on link
                            handleNotificationNavigation(notification, navController)
                            // Mark as read if unread
                            if (notification.read == 0) {
                                viewModel.markAsRead(notification.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val isUnread = notification.read == 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isUnread) 1.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Notification icon based on type
            Text(
                text = getNotificationIcon(notification.type),
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = com.strangerhelp.app.utils.TimeUtils.getTimeAgo(notification.created_at),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

fun getNotificationIcon(type: String): String = when (type) {
    "new_message" -> "💬"
    "claim_request" -> "📋"
    "claim_approved" -> "✅"
    "claim_rejected" -> "❌"
    "proof_submitted" -> "📸"
    "proof_accepted" -> "✅"
    "proof_rejected" -> "❌"
    "task_completed" -> "🎉"
    "review_received" -> "⭐"
    else -> "🔔"
}

fun extractTaskIdFromLink(link: String, notificationType: String = ""): String? {
    val trimmed = link.trim().removeSurrounding("\"").removeSurrounding("'")
    if (trimmed.isNotBlank()) {
        val clean = if (trimmed.contains("://")) {
            trimmed.substringAfter("://").substringAfter("/", "")
        } else {
            trimmed
        }.trim().trimStart('/')

        val withoutQuery = clean.substringBefore('?').substringBefore('#').trimEnd('/')
        val segments = withoutQuery.split('/').filter { it.isNotBlank() }

        // Find "tasks" or "task" segment
        val taskIndex = segments.indexOfFirst { it.equals("tasks", ignoreCase = true) || it.equals("task", ignoreCase = true) }
        if (taskIndex != -1 && taskIndex + 1 < segments.size) {
            val id = segments[taskIndex + 1].trim()
            if (id.isNotBlank()) return id
        }

        // Check if query parameter has taskId or id
        if (trimmed.contains("taskId=", ignoreCase = true)) {
            val id = trimmed.substringAfter("taskId=").substringBefore('&').substringBefore('#').trim()
            if (id.isNotBlank()) return id
        }
        if (trimmed.contains("id=", ignoreCase = true)) {
            val id = trimmed.substringAfter("id=").substringBefore('&').substringBefore('#').trim()
            if (id.isNotBlank()) return id
        }

        // If link itself is just the ID and notification type is task-related
        if (segments.size == 1 && !segments[0].equals("feed", ignoreCase = true) && !segments[0].equals("chat", ignoreCase = true)) {
            val candidate = segments[0].trim()
            if (candidate.isNotBlank() && (
                notificationType.startsWith("task", ignoreCase = true) ||
                notificationType.contains("claim", ignoreCase = true) ||
                notificationType.contains("proof", ignoreCase = true) ||
                notificationType.contains("review", ignoreCase = true)
            )) {
                return candidate
            }
        }
    }
    return null
}

fun extractConversationIdFromLink(link: String): String? {
    val trimmed = link.trim().removeSurrounding("\"").removeSurrounding("'")
    if (trimmed.isBlank()) return null
    val clean = if (trimmed.contains("://")) {
        trimmed.substringAfter("://").substringAfter("/", "")
    } else {
        trimmed
    }.trim().trimStart('/')
    val withoutQuery = clean.substringBefore('?').substringBefore('#').trimEnd('/')
    val segments = withoutQuery.split('/').filter { it.isNotBlank() }
    val chatIndex = segments.indexOfFirst { it.equals("chat", ignoreCase = true) || it.equals("messages", ignoreCase = true) }
    if (chatIndex != -1 && chatIndex + 1 < segments.size) {
        return segments[chatIndex + 1].trim()
    }
    return null
}

fun handleNotificationNavigation(
    notification: Notification,
    navController: NavController
) {
    val rawLink = notification.link.trim()
    val taskId = extractTaskIdFromLink(rawLink, notification.type)
    if (taskId != null) {
        navController.navigate("task/$taskId")
        return
    }

    val convId = extractConversationIdFromLink(rawLink)
    if (convId != null) {
        navController.navigate("chat/$convId")
        return
    }

    if (rawLink.isNotBlank()) {
        com.strangerhelp.app.navigation.DeepLinkHandler.handleLink(rawLink)
    } else {
        navController.navigate("feed")
    }
}
