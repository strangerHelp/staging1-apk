import os

filepath = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'

content = """package com.strangerhelp.app.ui.screens.tasks

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
                title = { 
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(android.R.drawable.ic_dialog_map), contentDescription = null, tint = CyanDeep, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("strangerhelp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, "More") } }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding)) { TaskDetailShimmer() }
        } else if (task == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Task not found or private", color = Muted) }
        } else {
            val t = task!!
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                
                // Map Header Area
                Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                    // Simulated Map Image
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0))) {
                       if (t.lat != null && t.lng != null) {
                           com.strangerhelp.app.ui.components.TaskMiniMap(t.lat, t.lng, t.location)
                       }
                    }
                    
                    // Verified Requester Badge
                    Card(
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, "Verified", tint = CyanDeep)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Verified Requester", fontWeight = FontWeight.Bold, color = CyanDeep, fontSize = 12.sp)
                                Text("Joined 2022", color = Muted, fontSize = 10.sp)
                            }
                        }
                    }

                    // In Progress Badge
                    Card(
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp).offset(y = 16.dp),
                        shape = RoundedCornerShape(50),
                        colors = CardDefaults.cardColors(containerColor = Warning)
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(android.R.drawable.ic_menu_rotate), contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("In Progress", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(t.title.ifEmpty { "Help moving heavy desk up one flight of stairs" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    // Meta info row
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, "Location", tint = Muted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Text("1.2 mi", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("away", color = Muted, fontSize = 14.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccessTime, "Time", tint = Muted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Text(t.deadline.ifEmpty { "Today, 4:00 PM" }, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AttachMoney, "Money", tint = CyanDeep, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Text("$${t.budget}", fontWeight = FontWeight.Bold, color = CyanDeep, fontSize = 16.sp)
                                Text("Reward", color = CyanDeep, fontSize = 14.sp)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Profile Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Gray))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(t.posterName.ifEmpty { "Alex Mercer" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Star, "Rating", tint = Warning, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("4.9 (24 tasks)", color = Muted, fontSize = 14.sp)
                                    }
                                }
                            }
                            IconButton(onClick = {}, modifier = Modifier.background(Color.White, CircleShape)) {
                                Icon(Icons.Filled.ChatBubbleOutline, "Chat")
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text("Details", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        t.description.ifEmpty { "I need help carrying a solid oak desk up one flight of stairs in my apartment building. It's quite heavy, requiring at least two strong people. I will provide gripping gloves. Please arrive on time as I have to leave for work shortly after." },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Audio Player
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(48.dp).background(Warning, CircleShape)
                            ) {
                                Icon(Icons.Filled.PlayArrow, "Play", tint = Color.White)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("0:00", fontSize = 12.sp, color = Muted)
                                    Text("0:45", fontSize = 12.sp, color = Muted)
                                }
                                Spacer(Modifier.height(4.dp))
                                // Fake waveform
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.width(4.dp).height(8.dp).background(Color.Gray, CircleShape))
                                    Box(Modifier.width(4.dp).height(12.dp).background(Color.Gray, CircleShape))
                                    Box(Modifier.width(4.dp).height(20.dp).background(Color.Gray, CircleShape))
                                    Box(Modifier.width(4.dp).height(24.dp).background(Color.Gray, CircleShape))
                                    Box(Modifier.width(4.dp).height(16.dp).background(Color.Gray, CircleShape))
                                    Box(Modifier.width(4.dp).height(28.dp).background(Warning, CircleShape))
                                    Box(Modifier.width(4.dp).height(12.dp).background(Color.LightGray, CircleShape))
                                    Box(Modifier.width(4.dp).height(8.dp).background(Color.LightGray, CircleShape))
                                    Box(Modifier.width(4.dp).height(10.dp).background(Color.LightGray, CircleShape))
                                    Box(Modifier.width(4.dp).height(6.dp).background(Color.LightGray, CircleShape))
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text("Photos (2)", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(Color.Gray))
                        Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(Color.Gray))
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
                
                // Task Actions Bottom Area
                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)
                ) {
                    Text("Task Actions", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)) // lighter gray
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.CameraAlt, "Camera", modifier = Modifier.size(32.dp), tint = Muted)
                            Spacer(Modifier.height(16.dp))
                            Text("Submit Proof of Completion", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Upload photos to verify the task is done to claim your reward.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Muted,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { cameraLauncher() },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Upload, "Upload", modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Submit Proof", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
"""

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Rewrite complete")
