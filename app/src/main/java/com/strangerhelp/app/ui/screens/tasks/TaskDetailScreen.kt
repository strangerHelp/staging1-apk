package com.strangerhelp.app.ui.screens.tasks

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*
import com.strangerhelp.app.ui.components.shimmerEffect
import com.strangerhelp.app.ui.components.rememberGpsCameraLauncher
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody

@Composable
fun TaskDetailShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(Modifier.fillMaxWidth(0.8f).height(32.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(80.dp, 24.dp).clip(RoundedCornerShape(50)).shimmerEffect())
            Box(Modifier.size(100.dp, 24.dp).clip(RoundedCornerShape(50)).shimmerEffect())
        }
        Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
        Box(Modifier.fillMaxWidth(0.5f).height(24.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
        Box(Modifier.fillMaxWidth(0.6f).height(24.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(navController: NavController, user: User, taskId: String, inviteCode: String? = null) {
    var task by remember { mutableStateOf<Task?>(null) }
    var loading by remember { mutableStateOf(true) }
    var claiming by remember { mutableStateOf(false) }
    var completing by remember { mutableStateOf(false) }
    var trackingToggleLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = com.strangerhelp.app.ui.components.LocalSnackbarHostState.current

    fun fetchTask() {
        scope.launch {
            try {
                val res = ApiClient.api.getTask(taskId, inviteCode)
                if (res.isSuccessful) task = res.body()
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(taskId) { fetchTask() }

    // Live Tracking Polling for Poster
    LaunchedEffect(task?.trackingActive) {
        if (task?.trackingActive == true && task?.posterId == user.id) {
            while (true) {
                delay(5000)
                try {
                    val res = ApiClient.api.getTask(taskId, inviteCode)
                    if (res.isSuccessful) task = res.body()
                } catch (_: Exception) {}
            }
        }
    }

    val cameraLauncher = rememberGpsCameraLauncher { file ->
        if (file != null) {
            completing = true
            scope.launch {
                try {
                    val actionReq = "complete".toRequestBody("text/plain".toMediaType())
                    val fileReq = file.asRequestBody("image/jpeg".toMediaType())
                    val part = MultipartBody.Part.createFormData("proof", file.name, fileReq)
                    
                    val res = ApiClient.api.completeTask(taskId, actionReq, part)
                    if (res.isSuccessful) {
                        snackbarHostState.showSnackbar("Proof submitted successfully")
                    }
                } catch (_: Exception) {}
                fetchTask()
                completing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Detail") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding)) { TaskDetailShimmer() }
        } else if (task == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Task not found or private", color = Muted) }
        } else {
            val t = task!!
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
                // Status + Category
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(t.category) }, shape = RoundedCornerShape(8.dp))
                    AssistChip(onClick = {}, label = { Text(t.status.replaceFirstChar { it.uppercase() }) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = when(t.status) { "open" -> CyanDeep.copy(0.1f); "claimed" -> Warning.copy(0.1f); else -> Muted.copy(0.1f) }),
                        shape = RoundedCornerShape(8.dp))
                    if (t.urgent == 1) AssistChip(onClick = {}, label = { Text("⚡ Urgent") }, colors = AssistChipDefaults.assistChipColors(containerColor = Error.copy(0.1f)), shape = RoundedCornerShape(8.dp))
                    if (t.visibility == "private") AssistChip(onClick = {}, label = { Text("🔒 Private") }, shape = RoundedCornerShape(8.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(t.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (t.description.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(t.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(24.dp))
                
                com.strangerhelp.app.ui.components.TaskStatusStepper(t.status)
                
                Spacer(Modifier.height(16.dp))
                // Info grid
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard("Budget", "₹${t.budget}${if(t.maxClaimers>1) "/person" else ""}", Modifier.weight(1f))
                    InfoCard("Deadline", t.deadline, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard("Location", t.location, Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                
                // Maps / Tracking
                if (t.trackingActive && t.helperLat != null && t.helperLng != null && t.posterId == user.id) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CyanDeep.copy(alpha = 0.1f))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("📍 Live Tracking Active", fontWeight = FontWeight.Bold, color = CyanDeep)
                            Text("Helper is sharing their location.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    com.strangerhelp.app.ui.components.TaskMiniMap(t.helperLat, t.helperLng, "Helper's Live Location")
                } else if (t.lat != null && t.lng != null) {
                    com.strangerhelp.app.ui.components.TaskMiniMap(t.lat, t.lng, t.location)
                }
                
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard("Posted by", if(t.anonymous==1) "Anonymous" else t.posterName, Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                
                // Actions
                if (t.status == "open" && t.posterId != user.id) {
                    Button(
                        onClick = {
                            claiming = true
                            scope.launch {
                                try { 
                                    val claimRes = ApiClient.api.claimTask(taskId, mapOf("action" to "claim")) 
                                    if (claimRes.isSuccessful) {
                                        snackbarHostState.showSnackbar("Task successfully claimed!")
                                    }
                                } catch (_: Exception) {}
                                fetchTask()
                                claiming = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        enabled = !claiming,
                    ) {
                        if (claiming) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text("Claim This Task", fontWeight = FontWeight.SemiBold)
                    }
                } else if (t.status == "claimed" && t.claimedBy == user.id) {
                    Column {
                        Text("✓ You claimed this task", color = CyanDeep, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(16.dp))
                        
                        // Start/Stop Live Tracking Button
                        Button(
                            onClick = {
                                trackingToggleLoading = true
                                scope.launch {
                                    val action = if (t.trackingActive) "stop_tracking" else "start_tracking"
                                    try {
                                        ApiClient.api.updateTracking(taskId, mapOf("action" to action))
                                        if (action == "start_tracking") {
                                            val intent = Intent(context, com.strangerhelp.app.service.TrackingService::class.java).apply {
                                                putExtra("taskId", taskId)
                                            }
                                            context.startForegroundService(intent)
                                        } else {
                                            context.stopService(Intent(context, com.strangerhelp.app.service.TrackingService::class.java))
                                        }
                                    } catch (_: Exception) {}
                                    fetchTask()
                                    trackingToggleLoading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            enabled = !trackingToggleLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = if (t.trackingActive) Error else MaterialTheme.colorScheme.primary)
                        ) {
                            if (trackingToggleLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            else Text(if (t.trackingActive) "Stop Live Tracking" else "Start Live Tracking", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(12.dp))
                        
                        Button(
                            onClick = { cameraLauncher() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            enabled = !completing,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanDeep)
                        ) {
                            if (completing) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            else Text("Mark as Completed (GPS Photo)", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else if (t.status == "claimed") {
                    Text("✓ Claimed by ${t.claimedByName ?: "a helper"}", color = Muted)
                } else if (t.status == "completed") {
                    if (t.posterId == user.id) {
                        val prefs = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("strangerhelp_prefs", android.content.Context.MODE_PRIVATE)
                        val ratingKey = "rating_${t._id}"
                        var rating by remember { mutableStateOf(prefs.getInt(ratingKey, 0)) }
                        var showRatingDialog by remember { mutableStateOf(false) }
                        
                        if (rating == 0) {
                            Button(
                                onClick = { showRatingDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanDeep)
                            ) {
                                Text("Rate Helper's Performance", fontWeight = FontWeight.SemiBold)
                            }
                            
                            if (showRatingDialog) {
                                var tempRating by remember { mutableStateOf(0) }
                                AlertDialog(
                                    onDismissRequest = { showRatingDialog = false },
                                    title = { Text("Rate Helper") },
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("How was the helper's performance?", style = MaterialTheme.typography.bodyMedium)
                                            Spacer(Modifier.height(16.dp))
                                            com.strangerhelp.app.ui.components.StarRating(
                                                rating = tempRating,
                                                onRatingChange = { tempRating = it }
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                if (tempRating > 0) {
                                                    rating = tempRating
                                                    prefs.edit().putInt(ratingKey, tempRating).apply()
                                                    showRatingDialog = false
                                                }
                                            },
                                            enabled = tempRating > 0
                                        ) {
                                            Text("Submit")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showRatingDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("You rated the helper:", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                Spacer(Modifier.height(4.dp))
                                com.strangerhelp.app.ui.components.StarRating(
                                    rating = rating,
                                    onRatingChange = {},
                                    readOnly = true,
                                    starSize = 24.dp
                                )
                            }
                        }
                    } else {
                        Text("✓ Task completed", color = Muted)
                    }
                }
                
                // Chat / Message Poster
                if (t.posterId != user.id) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val res = ApiClient.api.createConversation(mapOf("recipientId" to t.posterId, "taskId" to taskId))
                                    if (res.isSuccessful) {
                                        navController.navigate("chat/${res.body()?._id}")
                                    }
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text("Message Poster")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}
