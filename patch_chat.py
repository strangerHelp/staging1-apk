import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt'
content = """package com.strangerhelp.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    // Using mock data to match the UI if no messages exist or for demonstration
    val displayMessages = remember(messages) {
        if (messages.isEmpty()) {
            listOf(
                Message(
                    _id = "1",
                    senderId = "other",
                    senderName = "Elena Rodriguez",
                    text = "Hi there! I saw your post about needing help carrying groceries up to the 4th floor.",
                    createdAt = "2023-10-27T09:41:00Z"
                ),
                Message(
                    _id = "2",
                    senderId = user.id,
                    senderName = user.name,
                    text = "Yes, thank you! I sprained my ankle yesterday and the elevator in my building is out of service. 😅",
                    createdAt = "2023-10-27T09:42:00Z"
                ),
                Message(
                    _id = "3",
                    senderId = "other",
                    senderName = "Elena Rodriguez",
                    text = "Oh no, that sounds awful. I'm actually in your neighborhood right now. Does around 10:30 work for you?",
                    type = "location",
                    createdAt = "2023-10-27T09:45:00Z"
                ),
                Message(
                    _id = "4",
                    senderId = user.id,
                    senderName = user.name,
                    text = "10:30 is perfect! I'll buzz you in when you get here. My apartment is 4B.",
                    createdAt = "2023-10-27T09:46:00Z"
                )
            )
        } else messages
    }
    
    val otherUserName = displayMessages.firstOrNull { it.senderId != user.id }?.senderName ?: "Elena Rodriguez"

    fun loadMessages() {
        scope.launch {
            try {
                val res = ApiClient.api.getMessages(convId)
                if (res.isSuccessful && res.body() != null) {
                    messages = res.body()!!
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadMessages()
        while(true) {
            delay(5000)
            loadMessages()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary)
                    }
                    
                    // Logo replacement for profile pic
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            Icon(Icons.Outlined.Handshake, contentDescription = null, tint = Saffron, modifier = Modifier.size(12.dp).padding(bottom = 2.dp))
                        }
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.Bold, fontSize = 6.sp)) { append("stranger") }
                            withStyle(SpanStyle(color = Saffron, fontWeight = FontWeight.Bold, fontSize = 6.sp)) { append("help") }
                        }.let { Text(it) }
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(otherUserName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                        Text("Online now", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280)) // Gray
                    }
                    
                    IconButton(onClick = { /* Call */ }) {
                        Icon(Icons.Filled.Phone, "Call", tint = Color(0xFF6B7280))
                    }
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Filled.MoreVert, "More", tint = Color(0xFF6B7280))
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // + icon
                    Box(
                        modifier = Modifier.size(36.dp).border(1.dp, Color(0xFFD1D5DB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, "Add", tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message...", color = Color(0xFF9CA3AF)) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD1D5DB),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Saffron),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(padding).padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Today, 9:41 AM",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
            
            items(displayMessages) { msg ->
                val isMe = msg.senderId == user.id
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) Primary else Color(0xFFE5E7EB)
                        ),
                        modifier = Modifier.widthIn(max = 280.dp),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text, 
                                color = if(isMe) Color(0xFF9CA3AF) else Primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (msg.type == "location") {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    color = Color(0xFFD1D5DB),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Current Location Shared", color = Primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                    
                    if (isMe) {
                        Spacer(Modifier.height(2.dp))
                        Icon(
                            Icons.Filled.DoneAll, 
                            contentDescription = "Read", 
                            tint = Color(0xFF10B981), // Emerald Green
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
"""

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated ChatDetailScreen.kt")
