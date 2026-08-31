package com.strangerhelp.app.ui.screens.meets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.ui.screens.meets.MeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetDetailScreen(
    meetId: String,
    viewModel: MeetViewModel,
    navController: NavController
) {
    val meet by viewModel.selectedMeet.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(meetId) {
        viewModel.loadMeet(meetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meet Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    meet?.let { m ->
                        if (currentUser?.id == m.hostId) {
                            IconButton(onClick = {
                                viewModel.deleteMeet(m.id)
                                navController.popBackStack()
                            }) {
                                Icon(Icons.Default.Delete, "Delete", tint = com.strangerhelp.app.ui.screens.meets.Warning)
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && meet == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌", fontSize = 48.sp)
                    Text(error ?: "Meet not found", fontSize = 16.sp, color = Body)
                    Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
                }
            }
        } else {
            meet?.let { m ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { MeetInfoCard(meet = m) }
                    
                    if (m.description.isNotEmpty()) {
                        item { Text(text = m.description, fontSize = 14.sp, color = Body, modifier = Modifier.padding(vertical = 4.dp)) }
                    }
                    
                    item { AttendeesSection(attendees = m.attendees, attendeeCount = m.attendeeCount, maxAttendees = m.maxAttendees) }
                    
                    if (m.visibility == "private" && currentUser?.id == m.hostId) {
                        item { PrivateInviteLink(meetId = m.id, inviteCode = m.inviteCode ?: "") }
                    }
                    
                    item {
                        MeetActions(
                            meet = m,
                            currentUser = currentUser,
                            onJoin = { viewModel.joinMeet(m.id) },
                            onLeave = { viewModel.leaveMeet(m.id) },
                            onMessageHost = { /* message host functionality */ },
                            onLoginClick = { navController.navigate("login") }
                        )
                    }
                }
            }
        }
    }
}
