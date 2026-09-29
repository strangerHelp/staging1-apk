package com.strangerhelp.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.strangerhelp.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    viewModel: ChatViewModel = viewModel(),
    navController: NavController
) {
    val messages by viewModel.messages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(conversationId) {
        android.util.Log.d("ChatDetail", "Received conversationId: $conversationId")
        if (conversationId.isNotEmpty()) {
            viewModel.loadMessages(conversationId)
            viewModel.startPolling(conversationId)
            viewModel.loadConversations()
        } else {
            navController.popBackStack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val conversations by viewModel.conversations.collectAsState()
    val otherParticipantName = remember(conversations, conversationId, currentUser) {
        val conv = conversations.find { it._id == conversationId }
        val userId = currentUser?.id ?: ""
        conv?.participantNames
            ?.filterIndexed { index, _ ->
                conv.participants.getOrNull(index) != userId
            }
            ?.firstOrNull() ?: "User"
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                title = {
                    Text(
                        text = otherParticipantName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                actions = {
                    val conv = conversations.find { it._id == conversationId }
                    TextButton(
                        onClick = {
                            conv?.taskId?.let { taskId ->
                                navController.navigate("task/$taskId")
                            }
                        }
                    ) {
                        Text("View Task", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundLight
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                
                
                if (isLoading && messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                } else if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💬", fontSize = 32.sp)
                            Text(
                                text = "No messages yet",
                                fontSize = 14.sp,
                                color = Muted,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "Say hello to start the conversation!",
                                fontSize = 12.sp,
                                color = Muted
                            )
                        }
                    }
                } else {
                    items(messages, key = { it._id }) { message ->
                        MessageBubble(
                            message = message,
                            isOwn = message.senderId == currentUser?.id
                        )
                    }
                }
            }

            QuickReplies { reply ->
                inputText = reply
            }

            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(conversationId, inputText)
                        inputText = ""
                    }
                },
                onAttach = { imageFile ->
                    viewModel.sendMessageWithImage(conversationId, inputText, imageFile)
                    inputText = ""
                },
                isSending = isSending,
                maxLength = 5000
            )

            if (error != null) {
                LaunchedEffect(error) {
                    error?.let { snackbarHostState.showSnackbar(it) }
                    viewModel.clearError()
                }
            }
        }
    }
}
