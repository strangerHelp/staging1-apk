package com.strangerhelp.app.ui.screens.path

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Timer
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
import com.strangerhelp.app.data.model.Path
import com.strangerhelp.app.data.model.PathTask




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathActiveScreen(
    navController: NavController,
    viewModel: PathViewModel = viewModel()
) {
    val path by viewModel.path.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPath()
        viewModel.startAutoRefresh()
    }

    LaunchedEffect(path) {
        if (path == null && !isLoading) {
            navController.popBackStack("path_setup", inclusive = false)
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("My Path", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.clearPath() }) {
                        Text("Clear", color = TextColor, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { padding ->
        if (isLoading && path == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PathStatusCard(path = path, onRefresh = { viewModel.loadPath() })
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${tasks.size} tasks found along your route",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColor
                        )
                    }
                }

                if (tasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardOutlineColor)
                        ) {
                            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No tasks found yet.", color = MutedText, fontSize = 15.sp)
                            }
                        }
                    }
                } else {
                    items(tasks) { task ->
                        PathTaskCard(
                            task = task,
                            onClick = { navController.navigate("task/${task.id}") }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.clearPath() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Path", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun PathStatusCard(path: Path?, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardOutlineColor)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left Accent
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(CyanColor)
            )
            
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CyanColor))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Path\nActive", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextColor, lineHeight = 24.sp)
                    }
                    
                    Surface(
                        color = Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Expires in 21h\n15m", fontSize = 12.sp, color = TextColor, lineHeight = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                path?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⎰⎱", fontSize = 16.sp, color = MutedText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${it.fromLocation} → ${it.toLocation}",
                            fontSize = 15.sp,
                            color = TextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp), tint = MutedText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Radius: ${"%.1f".format(it.radiusKm)} km",
                            fontSize = 15.sp,
                            color = TextColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = TextColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Tasks", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PathTaskCard(task: PathTask, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardOutlineColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (task.status == "open") Color(0xFF2196F3) else PrimaryColor))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextColor
                    )
                }
                Text(
                    text = "₹${task.budget}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(6.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(14.dp), tint = MutedText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${"%.1f".format(task.offPath)} km off route", fontSize = 12.sp, color = MutedText)
                    }
                }
                Surface(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(6.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DirectionsWalk, contentDescription = null, modifier = Modifier.size(14.dp), tint = MutedText)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${"%.1f".format(task.distFromStart)} km from start", fontSize = 12.sp, color = MutedText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View Task", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
