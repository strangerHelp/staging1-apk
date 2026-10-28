package com.strangerhelp.app.ui.screens.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.navigation.DeepLinkHandler
import com.strangerhelp.app.service.NotificationChannels
import com.strangerhelp.app.util.NotificationBuilder
import com.strangerhelp.app.util.NotificationTester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Developer Testing Screen for FCM Notifications.
 *
 * Allows developers and QA to:
 * 1. Dispatch realistic preset FCM mock payloads (claim_request, new_message, proof_submitted, etc.)
 * 2. Simulate delayed notifications to test background/lockscreen/cold-start behavior
 * 3. Compose custom FCM payloads and preview the JSON schema
 * 4. Verify channel routing (TASKS, MESSAGES, PROOF, PROMOTIONS, DEFAULT)
 * 5. Verify and debug deep-linking routes without requiring a backend server
 */
data class MockFcmPayload(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val link: String,
    val iconEmoji: String,
    val expectedChannel: String,
    val description: String
)

val PRESET_MOCK_PAYLOADS = listOf(
    MockFcmPayload(
        id = "claim_req",
        type = "claim_request",
        title = "New Claim Request",
        body = "Rahul K. offered to help with 'Grocery & Medicine Delivery'. Tap to review.",
        link = "/tasks/8559f42316dd3a3f3d3080ba",
        iconEmoji = "📋",
        expectedChannel = NotificationChannels.TASKS,
        description = "Sent to poster when a helper requests to claim their task"
    ),
    MockFcmPayload(
        id = "claim_appr",
        type = "claim_approved",
        title = "Claim Approved! 🎉",
        body = "Your claim for 'Bike Service & Delivery' was approved. Head to the location.",
        link = "/tasks/8559f42316dd3a3f3d3080ba",
        iconEmoji = "✅",
        expectedChannel = NotificationChannels.TASKS,
        description = "Sent to helper when the task owner approves their claim"
    ),
    MockFcmPayload(
        id = "claim_rej",
        type = "claim_rejected",
        title = "Claim Update",
        body = "The poster selected another helper for 'Document Submission'. Browse more tasks.",
        link = "/tasks",
        iconEmoji = "❌",
        expectedChannel = NotificationChannels.TASKS,
        description = "Sent to helper if their claim was not selected"
    ),
    MockFcmPayload(
        id = "new_msg",
        type = "new_message",
        title = "New Message from Amit",
        body = "I'm waiting near the gate entrance. Which tower should I deliver to?",
        link = "/chat/conv_123",
        iconEmoji = "💬",
        expectedChannel = NotificationChannels.MESSAGES,
        description = "Sent when a new direct chat message arrives"
    ),
    MockFcmPayload(
        id = "proof_sub",
        type = "proof_submitted",
        title = "Photo Proof Uploaded 📸",
        body = "Helper submitted photo proof of completion. Review and confirm payout.",
        link = "/tasks/8559f42316dd3a3f3d3080ba",
        iconEmoji = "📸",
        expectedChannel = NotificationChannels.PROOF,
        description = "Sent to poster when helper uploads task photo proof"
    ),
    MockFcmPayload(
        id = "proof_acc",
        type = "proof_accepted",
        title = "Proof Accepted — Payment Released! 💰",
        body = "₹350 has been credited to your StrangerHelp wallet.",
        link = "/tasks/8559f42316dd3a3f3d3080ba",
        iconEmoji = "💰",
        expectedChannel = NotificationChannels.PROOF,
        description = "Sent to helper when task poster verifies proof"
    ),
    MockFcmPayload(
        id = "proof_rej",
        type = "proof_rejected",
        title = "Proof Clarification Requested ⚠️",
        body = "Poster requested a clearer receipt photo. Tap to resubmit proof.",
        link = "/tasks/8559f42316dd3a3f3d3080ba",
        iconEmoji = "⚠️",
        expectedChannel = NotificationChannels.PROOF,
        description = "Sent to helper if proof photo needs adjustment"
    ),
    MockFcmPayload(
        id = "task_comp",
        type = "task_completed",
        title = "Task Completed Successfully 🏆",
        body = "Task 'Water Plant & Pet Check' is marked complete. Don't forget to leave a review.",
        link = "/tasks/8559f42316dd3a3f3d3080ba",
        iconEmoji = "🏆",
        expectedChannel = NotificationChannels.TASKS,
        description = "Sent when the task lifecycle reaches completion"
    ),
    MockFcmPayload(
        id = "rev_recv",
        type = "review_received",
        title = "5-Star Review Received ⭐",
        body = "'Super quick and reliable helper!' - Tap to view your updated profile rating.",
        link = "/user/user_456",
        iconEmoji = "⭐",
        expectedChannel = NotificationChannels.PROOF,
        description = "Sent to user when they receive feedback"
    ),
    MockFcmPayload(
        id = "promo",
        type = "promotion",
        title = "Weekend Helper Surge Active! 🎁",
        body = "Earn +25% bonus coins on all completed community requests today.",
        link = "/tasks",
        iconEmoji = "🎁",
        expectedChannel = NotificationChannels.PROMOTIONS,
        description = "Low-priority marketing / tips channel"
    ),
    MockFcmPayload(
        id = "ask_ans",
        type = "ask_answer",
        title = "New Answer to Your Question 💡",
        body = "Kiran answered: 'The local municipal office opens at 9:30 AM.'",
        link = "/ask_detail/q_101",
        iconEmoji = "💡",
        expectedChannel = NotificationChannels.TASKS,
        description = "Sent when a neighbor answers your Ask question"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDevTestScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0: Presets, 1: Custom Builder, 2: Channel Inspector
    var hasPermission by remember { mutableStateOf(NotificationTester.isNotificationPermissionGranted(context)) }
    var countdownSeconds by remember { mutableStateOf(0) }

    // Custom Builder States
    var customType by remember { mutableStateOf("new_message") }
    var customTitle by remember { mutableStateOf("New message from Priya") }
    var customBody by remember { mutableStateOf("I've arrived at your location with the documents.") }
    var customLink by remember { mutableStateOf("/chat/conv_123") }
    var customDelaySeconds by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied; notifications cannot post to status bar.", Toast.LENGTH_LONG).show()
        }
    }

    fun resolveChannelForType(type: String): String = NotificationChannels.resolve(type)

    fun triggerMockNotification(
        title: String,
        body: String,
        link: String,
        type: String,
        delaySec: Int = 0
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        NotificationChannels.createAll(context)
        val channelId = resolveChannelForType(type)
        val notificationId = (System.currentTimeMillis() % 100000).toInt()

        if (delaySec <= 0) {
            NotificationBuilder.show(
                context = context,
                title = title,
                body = body,
                link = link,
                channelId = channelId,
                notificationId = notificationId
            )
            Toast.makeText(context, "Mock FCM dispatched via [$channelId]!", Toast.LENGTH_SHORT).show()
        } else {
            coroutineScope.launch {
                countdownSeconds = delaySec
                Toast.makeText(context, "Notification scheduled in ${delaySec}s. Press Home to test background!", Toast.LENGTH_SHORT).show()
                while (countdownSeconds > 0) {
                    delay(1000)
                    countdownSeconds--
                }
                NotificationBuilder.show(
                    context = context,
                    title = title,
                    body = body,
                    link = link,
                    channelId = channelId,
                    notificationId = notificationId
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("FCM Payload Simulator", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Test NotificationBuilder & Deep Links", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("btn_dev_test_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                        IconButton(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            modifier = Modifier.testTag("btn_request_notif_perm")
                        ) {
                            Icon(Icons.Outlined.WarningAmber, contentDescription = "Grant Permission", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Permission & Countdown banner
            if (countdownSeconds > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dispatching notification in $countdownSeconds s... (Switch apps to test)",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Presets (${PRESET_MOCK_PAYLOADS.size})") },
                    icon = { Icon(Icons.Outlined.Layers, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Custom Builder") },
                    icon = { Icon(Icons.Outlined.Build, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Channels") },
                    icon = { Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Crash Tests 🛡️") },
                    icon = { Icon(Icons.Outlined.Shield, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Presets Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Tap 'Trigger' to fire an Android system push. Pull down the status bar and tap the notification to test its deep link routing.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        items(PRESET_MOCK_PAYLOADS, key = { it.id }) { payload ->
                            PresetPayloadCard(
                                payload = payload,
                                onTriggerInstant = {
                                    triggerMockNotification(payload.title, payload.body, payload.link, payload.type, delaySec = 0)
                                },
                                onTriggerDelayed = {
                                    triggerMockNotification(payload.title, payload.body, payload.link, payload.type, delaySec = 4)
                                },
                                onTestDeepLinkOnly = {
                                    DeepLinkHandler.handleLink(payload.link)
                                    Toast.makeText(context, "Navigating directly via DeepLink: ${payload.link}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // Custom Builder Tab
                    CustomPayloadBuilder(
                        type = customType,
                        onTypeChange = { customType = it },
                        title = customTitle,
                        onTitleChange = { customTitle = it },
                        body = customBody,
                        onBodyChange = { customBody = it },
                        link = customLink,
                        onLinkChange = { customLink = it },
                        delaySeconds = customDelaySeconds,
                        onDelaySecondsChange = { customDelaySeconds = it },
                        resolvedChannel = resolveChannelForType(customType),
                        onDispatch = {
                            triggerMockNotification(customTitle, customBody, customLink, customType, customDelaySeconds)
                        },
                        onTestDeepLinkOnly = {
                            DeepLinkHandler.handleLink(customLink)
                            Toast.makeText(context, "Deep link dispatched: $customLink", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                2 -> {
                    // Channel Inspector Tab
                    ChannelInspector(context = context)
                }
                3 -> {
                    // Crash & Resilience Testing Tab
                    CrashTestingPane(context = context)
                }
            }
        }
    }
}

@Composable
private fun PresetPayloadCard(
    payload: MockFcmPayload,
    onTriggerInstant: () -> Unit,
    onTriggerDelayed: () -> Unit,
    onTestDeepLinkOnly: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payload_card_${payload.type}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(payload.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = payload.type,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = payload.expectedChannel.removePrefix("strangerhelp_"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = payload.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = payload.body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = payload.link,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTriggerInstant,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_trigger_${payload.type}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trigger Push", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onTriggerDelayed,
                    modifier = Modifier.testTag("btn_delay_${payload.type}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delay 4s", fontSize = 12.sp)
                }

                FilledTonalIconButton(
                    onClick = onTestDeepLinkOnly,
                    modifier = Modifier.size(36.dp).testTag("btn_link_${payload.type}")
                ) {
                    Icon(Icons.Outlined.OpenInNew, contentDescription = "Test Deep Link", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomPayloadBuilder(
    type: String,
    onTypeChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    body: String,
    onBodyChange: (String) -> Unit,
    link: String,
    onLinkChange: (String) -> Unit,
    delaySeconds: Int,
    onDelaySecondsChange: (Int) -> Unit,
    resolvedChannel: String,
    onDispatch: () -> Unit,
    onTestDeepLinkOnly: () -> Unit
) {
    val presetTypes = listOf(
        "claim_request",
        "claim_approved",
        "claim_rejected",
        "new_message",
        "proof_submitted",
        "proof_accepted",
        "review_received",
        "task_completed",
        "promotion",
        "custom"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Payload Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Simulate any custom data payload format sent from backend FCM", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Text("Event Type (type)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetTypes.take(4).forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { onTypeChange(t) },
                        label = { Text(t, fontSize = 11.sp) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetTypes.drop(4).take(4).forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { onTypeChange(t) },
                        label = { Text(t, fontSize = 11.sp) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = type,
                onValueChange = onTypeChange,
                label = { Text("Custom Event Type") },
                modifier = Modifier.fillMaxWidth().testTag("input_custom_type"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Title (data.title)") },
                modifier = Modifier.fillMaxWidth().testTag("input_custom_title"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = body,
                onValueChange = onBodyChange,
                label = { Text("Body (data.body)") },
                modifier = Modifier.fillMaxWidth().testTag("input_custom_body"),
                maxLines = 3
            )
        }

        item {
            OutlinedTextField(
                value = link,
                onValueChange = onLinkChange,
                label = { Text("Deep Link (data.link)") },
                placeholder = { Text("/tasks/123 or /chat/456") },
                modifier = Modifier.fillMaxWidth().testTag("input_custom_link"),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = onTestDeepLinkOnly) {
                        Icon(Icons.Outlined.OpenInNew, contentDescription = "Test link directly")
                    }
                }
            )
        }

        item {
            Text("Dispatch Delay (For Background Testing)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0, 3, 6, 10).forEach { sec ->
                    FilterChip(
                        selected = delaySeconds == sec,
                        onClick = { onDelaySecondsChange(sec) },
                        label = { Text(if (sec == 0) "Instant" else "${sec}s Delay") }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Routing & Channel Target:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("Channel ID: $resolvedChannel", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Equivalent FCM HTTP v1 JSON:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text(
                            text = """{
  "message": {
    "data": {
      "type": "$type",
      "title": "$title",
      "body": "$body",
      "link": "$link"
    },
    "android": { "priority": "high" }
  }
}""",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF00FF66),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onDispatch,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_dispatch_custom_fcm")
            ) {
                Icon(Icons.Outlined.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (delaySeconds > 0) "Schedule Mock FCM (${delaySeconds}s delay)" else "Send Mock FCM Now")
            }
        }
    }
}

@Composable
private fun ChannelInspector(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    NotificationChannels.createAll(context)

    val channels = listOf(
        Triple(NotificationChannels.DEFAULT, "General Updates", "strangerhelp_default"),
        Triple(NotificationChannels.TASKS, "Task Updates & Claims", "strangerhelp_tasks"),
        Triple(NotificationChannels.MESSAGES, "Chat Direct Messages", "strangerhelp_messages"),
        Triple(NotificationChannels.PROOF, "Proof & Reviews", "strangerhelp_proof"),
        Triple(NotificationChannels.PROMOTIONS, "Promotions & Tips", "strangerhelp_promotions")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Notification Channels Inspector", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Verify Android system channel importance and system settings", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Button(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open system settings", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_open_system_notif_settings")
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Android App Notification Settings")
            }
        }

        items(channels) { (id, name, desc) ->
            val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.getNotificationChannel(id)
            } else null

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when (id) {
                                    NotificationChannels.PROMOTIONS -> "LOW (Silent)"
                                    NotificationChannels.DEFAULT -> "DEFAULT"
                                    else -> "HIGH (Heads-up)"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ID: $id", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channel != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sound: ${if (channel.sound != null) "Enabled" else "None"} • Vibration: ${if (channel.shouldVibrate()) "Enabled" else "Disabled"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                try {
                                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        putExtra(Settings.EXTRA_CHANNEL_ID, id)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to open channel settings", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Configure Channel in OS Settings", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CrashTestingPane(context: Context) {
    var suiteResult by remember { mutableStateOf<com.strangerhelp.app.util.CrashSuiteResult?>(null) }
    var isRunningSuite by remember { mutableStateOf(false) }
    var lastActionMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crash & Resilience Test Suite", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Stress-test notification building, oversized 50KB payloads, Unicode/emojis, malformed deep links, missing channels, and FGS permission safety to verify the app cannot be crashed by corrupted inputs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    isRunningSuite = true
                    coroutineScope.launch {
                        delay(200)
                        suiteResult = NotificationTester.runCrashResilienceTests(context)
                        isRunningSuite = false
                        lastActionMessage = "Full suite completed: ${suiteResult?.passedCount}/${suiteResult?.total} resilience tests passed!"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_run_crash_suite"),
                enabled = !isRunningSuite
            ) {
                if (isRunningSuite) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Running Stress & Crash Tests...")
                } else {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run All Automated Crash Tests")
                }
            }
        }

        if (lastActionMessage != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(lastActionMessage!!, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        suiteResult?.let { result ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.allPassed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Suite Result: ${if (result.allPassed) "ALL TESTS PASSED ✅" else "TESTS FAILED ⚠️"}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${result.passedCount} of ${result.total} resilience barriers verified", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = if (result.allPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "${result.passedCount}/${result.total}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            items(result.items, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Surface(
                                color = if (item.passed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (item.passed) "PASSED" else "FAILED",
                                    color = if (item.passed) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.details, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Individual Crash Simulation Triggers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Trigger individual edge-case pushes directly to your status bar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        NotificationTester.sendTestPushNotification(context, title = "", body = "", link = "")
                        lastActionMessage = "Dispatched empty string payload notification"
                    },
                    modifier = Modifier.weight(1f).testTag("btn_trigger_empty_crash")
                ) {
                    Text("Empty Strings", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        val huge = "StrangerHelp Stress Data ".repeat(500)
                        NotificationTester.sendTestPushNotification(context, title = "Oversized Test", body = huge, link = "/tasks")
                        lastActionMessage = "Dispatched 12KB BigTextStyle payload"
                    },
                    modifier = Modifier.weight(1f).testTag("btn_trigger_huge_crash")
                ) {
                    Text("12KB BigText", fontSize = 11.sp)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        NotificationTester.sendTestPushNotification(
                            context,
                            title = "Unicode: 🔔🔥🚀❤️\u0000\uFFFF",
                            body = "Glyphs: \uD83D\uDE00 \uD83D\uDE04 \uD83D\uDE02",
                            link = "/tasks"
                        )
                        lastActionMessage = "Dispatched Unicode emoji notification"
                    },
                    modifier = Modifier.weight(1f).testTag("btn_trigger_unicode_crash")
                ) {
                    Text("Unicode/Emoji", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        DeepLinkHandler.handleLink("strangerhelp://///??invalid=%%%")
                        lastActionMessage = "Parsed corrupted URI — safely handled"
                    },
                    modifier = Modifier.weight(1f).testTag("btn_trigger_malformed_link")
                ) {
                    Text("Bad DeepLink", fontSize = 11.sp)
                }
            }
        }
    }
}
