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
import com.strangerhelp.app.ui.components.shimmerEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.strangerhelp.app.StrangerHelpApp

@Composable
fun ConversationShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).shimmerEffect())
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth(0.8f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController, user: User) {
    val db = StrangerHelpApp.instance.database
    val conversations by db.conversationDao().getAllConversations().collectAsStateWithLifecycle(initialValue = emptyList())
    var loading by remember { mutableStateOf(true) }

    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            try {
                val res = ApiClient.api.getConversations()
                if (res.isSuccessful) {
                    val body = res.body() ?: emptyList()
                    db.conversationDao().insertConversations(body)
                }
            } catch (_: Exception) {}
            pullRefreshState.endRefresh()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.api.getConversations()
            if (res.isSuccessful) {
                val body = res.body() ?: emptyList()
                db.conversationDao().insertConversations(body)
            }
        } catch (_: Exception) {}
        loading = false
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullRefreshState.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Messages", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            if (loading && conversations.isEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(5) { ConversationShimmer() }
                }
            } else if (conversations.isEmpty()) {
                Text("No conversations yet", color = Muted, modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(conversations) { conv -> ConversationItem(conv, onClick = { navController.navigate("chat/${conv._id}") }) }
                }
            }
        }
        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun ConversationItem(conv: Conversation, onClick: () -> Unit) {
    val otherName = conv.participantNames.lastOrNull() ?: "User"
    val initials = otherName.take(2).uppercase()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
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
