package com.strangerhelp.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Message
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*
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

    fun loadMessages() {
        scope.launch {
            try {
                val res = ApiClient.api.getMessages(convId)
                if (res.isSuccessful) messages = res.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(convId) {
        loadMessages()
        // Poll for new messages
        while (true) { delay(5000); loadMessages() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Back") } }
            )
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
                                    ApiClient.api.sendMessage(convId, mapOf("text" to text))
                                    loadMessages()
                                } catch (_: Exception) {}
                                sending = false
                            }
                        },
                        enabled = inputText.isNotBlank() && !sending,
                    ) { Icon(Icons.Filled.Send, "Send") }
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
            items(messages) { msg ->
                val isMe = msg.senderId == user.id
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp, 16.dp, if(isMe) 4.dp else 16.dp, if(isMe) 16.dp else 4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.widthIn(max = 280.dp),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            if (!isMe) Text(msg.senderName, style = MaterialTheme.typography.labelSmall, color = CyanDeep)
                            Text(msg.text, color = if(isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
