package com.strangerhelp.app.ui.screens.notifications

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.Notification
import com.strangerhelp.app.util.NotificationTester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var hasNotificationPermission by remember {
        mutableStateOf(NotificationTester.isNotificationPermissionGranted(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            NotificationTester.sendTestPushNotification(context)
            viewModel.addTestNotification(
                title = "Task Claimed! (Test)",
                message = "A neighbor offered to help with your task. Tap to view.",
                type = "task_claimed"
            )
            Toast.makeText(context, "Test notification sent! Check your notification bar.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission is needed for status bar alerts.", Toast.LENGTH_LONG).show()
        }
    }

    fun triggerTestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            val sent = NotificationTester.sendTestPushNotification(context)
            viewModel.addTestNotification(
                title = "Task Claimed! (Test)",
                message = "A neighbor offered to help with your task. Tap to view.",
                type = "task_claimed"
            )
            if (sent) {
                Toast.makeText(context, "Test notification sent! Check your status bar.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Check app notification settings in Android Settings.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                    IconButton(
                        onClick = { triggerTestNotification() },
                        modifier = Modifier.testTag("btn_test_notification")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = "Send Test Notification",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                com.strangerhelp.app.ui.components.EmptyState(
                    icon = "🔔",
                    title = "No notifications yet",
                    message = "We'll notify you when someone claims your task or sends a message."
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Test notification card in empty state
                NotificationTestCard(
                    hasPermission = hasNotificationPermission,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onSendTest = { triggerTestNotification() }
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
                item {
                    // Diagnostic quick tester banner
                    NotificationTestCard(
                        hasPermission = hasNotificationPermission,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onSendTest = { triggerTestNotification() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

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
private fun NotificationTestCard(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSendTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasPermission) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = if (hasPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notification System Status",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = if (hasPermission) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (hasPermission) "Active" else "Permission Needed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (hasPermission) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Channel: strangerhelp_channel • Status bar push alerts & deep links",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    OutlinedButton(
                        onClick = onRequestPermission,
                        modifier = Modifier.weight(1f).testTag("btn_enable_notifications")
                    ) {
                        Text("Grant Permission", fontSize = 13.sp)
                    }
                }
                Button(
                    onClick = onSendTest,
                    modifier = Modifier.weight(1f).testTag("btn_send_test_notification")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Test Push", fontSize = 13.sp)
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
