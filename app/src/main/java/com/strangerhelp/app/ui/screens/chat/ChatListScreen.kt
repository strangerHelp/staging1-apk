package com.strangerhelp.app.ui.screens.chat

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
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Conversation
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.Muted

@Composable
fun ChatListScreen(navController: NavController, user: User) {
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.api.getConversations()
            if (res.isSuccessful) conversations = res.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Messages", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp))
        } else if (conversations.isEmpty()) {
            Text("No conversations yet", color = Muted, modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(conversations) { conv -> ConversationItem(conv) }
            }
        }
    }
}

@Composable
fun ConversationItem(conv: Conversation) {
    val otherName = conv.participantNames.lastOrNull() ?: "User"
    val initials = otherName.take(2).uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(initials, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(otherName, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(
                    conv.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
