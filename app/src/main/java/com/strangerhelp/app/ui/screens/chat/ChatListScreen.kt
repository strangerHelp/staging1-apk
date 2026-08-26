package com.strangerhelp.app.ui.screens.chat

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.Conversation
import com.strangerhelp.app.ui.theme.Body
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.SurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (isLoading && conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("💬", fontSize = 48.sp)
                    Text(
                        text = "No conversations yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Body
                    )
                    Text(
                        text = "Claim a task to start chatting with someone!",
                        fontSize = 13.sp,
                        color = Muted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(conversations, key = { it._id }) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        currentUserId = currentUser?.id ?: "",
                        onClick = {
                            android.util.Log.d("ChatList", "Clicked conversation ID: ${conversation._id}")
                            if (conversation._id.isNotEmpty()) {
                                navController.navigate("chat/${conversation._id}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit
) {
    val otherName = conversation.participantNames
        .filterIndexed { index, _ ->
            conversation.participants.getOrNull(index) != currentUserId
        }
        .firstOrNull() ?: "User"

    val initial = otherName.firstOrNull()?.uppercase() ?: "U"
    val timeAgo = com.strangerhelp.app.utils.TimeUtils.getTimeAgo(conversation.lastMessageAt)
    val lastMessage = conversation.lastMessage.ifEmpty { "No messages yet" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Body
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and last message
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = otherName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    Text(
                        text = timeAgo,
                        fontSize = 11.sp,
                        color = Muted
                    )
                }
                Text(
                    text = lastMessage,
                    fontSize = 13.sp,
                    color = Body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
