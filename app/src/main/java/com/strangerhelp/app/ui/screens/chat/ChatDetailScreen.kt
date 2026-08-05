package com.strangerhelp.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Message
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*
import com.strangerhelp.app.data.local.ChatSettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavController, user: User, convId: String) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    var previousMessageCount by remember { mutableStateOf(0) }
    
    val context = LocalContext.current
    val chatSettings = remember { ChatSettingsManager(context) }
    val chatTheme by chatSettings.themeFlow.collectAsState(initial = "system")
    val bubbleColorInt by chatSettings.bubbleColorFlow.collectAsState(initial = 0)
    
    var showSettings by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredMessages = if (searchQuery.isNotBlank()) {
        messages.filter { it.text.contains(searchQuery, ignoreCase = true) }
    } else {
        messages
    }

    fun loadMessages() {
        scope.launch {
            try {
                val res = ApiClient.api.getMessages(convId)
                if (res.isSuccessful) {
                    val newMessages = res.body() ?: emptyList()
                    if (newMessages.size > previousMessageCount && previousMessageCount > 0) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    previousMessageCount = newMessages.size
                    messages = newMessages
                }
            } catch (_: Exception) {}
        }
    }
    
    LaunchedEffect(convId) {
        loadMessages()
        while (true) { delay(5000); loadMessages() }
    }
    
    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty() && !isSearching) listState.animateScrollToItem(filteredMessages.size - 1)
    }

    val isDark = when (chatTheme) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    StrangerHelpTheme(darkTheme = isDark) {
        val bubbleColor = if (bubbleColorInt != 0) Color(bubbleColorInt) else MaterialTheme.colorScheme.primary
        
        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                if (isSearching) {
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search messages...") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearching = false 
                                searchQuery = ""
                            }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("Chat") },
                        actions = {
                            IconButton(onClick = { isSearching = true }) { Icon(Icons.Filled.Search, "Search") }
                            IconButton(onClick = { showSettings = !showSettings }) { Icon(Icons.Filled.Settings, "Settings") }
                            DropdownMenu(expanded = showSettings, onDismissRequest = { showSettings = false }) {
                                DropdownMenuItem(
                                    text = { Text("Theme: Light") },
                                    onClick = { scope.launch { chatSettings.saveTheme("light") }; showSettings = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Theme: Dark") },
                                    onClick = { scope.launch { chatSettings.saveTheme("dark") }; showSettings = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Theme: System") },
                                    onClick = { scope.launch { chatSettings.saveTheme("system") }; showSettings = false }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Bubble: Default") },
                                    onClick = { scope.launch { chatSettings.saveBubbleColor(0) }; showSettings = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bubble: Blue") },
                                    onClick = { scope.launch { chatSettings.saveBubbleColor(Color(0xFF006494).toArgb()) }; showSettings = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bubble: Green") },
                                    onClick = { scope.launch { chatSettings.saveBubbleColor(Color(0xFF2E7D32).toArgb()) }; showSettings = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bubble: Purple") },
                                    onClick = { scope.launch { chatSettings.saveBubbleColor(Color(0xFF6A1B9A).toArgb()) }; showSettings = false }
                                )
                            }
                        },
                        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
                    )
                }
            },
            bottomBar = {
                Surface(tonalElevation = 2.dp) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = inputText, onValueChange = { inputText = it },
                            placeholder = { Text("Type a message...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = {
                                if (inputText.isBlank() || sending) return@FilledIconButton
                                sending = true
                                val text = inputText; inputText = ""
                                scope.launch {
                                    try {
                                        val sendRes = ApiClient.api.sendMessage(convId, mapOf("text" to text))
                                        if (sendRes.isSuccessful) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        loadMessages()
                                    } catch (_: Exception) {}
                                    sending = false
                                }
                            },
                            enabled = inputText.isNotBlank() && !sending,
                        ) { Icon(Icons.AutoMirrored.Filled.Send, "Send") }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(filteredMessages) { msg ->
                    val isMe = msg.senderId == user.id
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp, 16.dp, if(isMe) 4.dp else 16.dp, if(isMe) 16.dp else 4.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isMe) bubbleColor else MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.widthIn(max = 280.dp),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                if (!isMe) Text(msg.senderName, style = MaterialTheme.typography.labelSmall, color = CyanDeep)
                                Text(msg.text, color = if(isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                val timeStr = try {
                                    if (msg.createdAt.isNotBlank() && msg.createdAt.contains("T")) {
                                        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                                        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                        val cleanTime = if (msg.createdAt.contains(".")) msg.createdAt.substringBefore(".") else msg.createdAt.replace("Z", "")
                                        val date = format.parse(cleanTime)
                                        if (date != null) {
                                            val outFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                            outFormat.format(date)
                                        } else {
                                            msg.createdAt.substringAfter("T").take(5)
                                        }
                                    } else ""
                                } catch (e: Exception) {
                                    ""
                                }
                                if (timeStr.isNotBlank()) {
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
