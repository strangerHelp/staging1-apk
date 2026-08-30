package com.strangerhelp.app.ui.screens.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.ClaimRequest
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.annotations.MarkerOptions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    user: User?, // Not used in favor of viewModel's user, but keeping for signature
    taskId: String,
    viewModel: TaskDetailViewModel = viewModel(factory = TaskDetailViewModelFactory())
) {
    val task by viewModel.task.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isClaiming by viewModel.isClaiming.collectAsStateWithLifecycle()
    val isSubmittingProof by viewModel.isSubmittingProof.collectAsStateWithLifecycle()
    val context = LocalContext.current
    

    
    var showRejectionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    LaunchedEffect(error) {
        if (!error.isNullOrEmpty()) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { task?.let { shareTask(navController.context, it) } }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    IconButton(onClick = { /* More Options */ }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && task == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentOrange)
            }
        } else {
            task?.let { t ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFFAFAFA)),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // 1. Task Info Card
                    item { TaskInfoCard(t) }

                    // 2. Description
                    if (t.description.isNotEmpty()) {
                        item { DescriptionSection(t.description) }
                    }

                    // 3. Info Grid
                    item { InfoGrid(t) }
                    
                    // 3.5. Task Lifecycle Stepper
                    item { 
                        Spacer(Modifier.height(16.dp))
                        TaskLifecycleStepper(t, currentUser?.id) 
                    }

                    // 4. Group Task Progress
                    if (t.maxClaimers > 1) {
                        item { GroupTaskProgress(t) }
                    }

                    // 5. Map Section
                    item { MapSection(t) }

                    // 6. Poster Info
                    item { PosterInfoCard(t) }

                    // 7. Claim Requests (Poster only)
                    if (currentUser?.id == t.posterId && t.status == "open") {
                        item {
                            ClaimRequestsSection(t, viewModel)
                        }
                    }

                    // 8. Action Buttons
                    item {
                        val isOwner = currentUser?.id == t.posterId
                        val isClaimer = currentUser?.id == t.claimedBy
                        
                        when {
                            isOwner -> PosterActions(t, viewModel, navController, onDelete = { showDeleteConfirmation = true }, onReview = { showReviewDialog = true })
                            isClaimer -> HelperActions(t, viewModel, navController, onSubmitProof = { viewModel.submitProof(t._id, emptyList()) }, onReview = { showReviewDialog = true })
                            currentUser != null && t.status == "open" -> {
                                VisitorActions(
                                    task = t,
                                    viewModel = viewModel,
                                    navController = navController,
                                    taskId = taskId
                                )
                            }
                            currentUser == null -> LoginPrompt(navController)
                            t.status == "completed" && !isOwner && !isClaimer -> { /* Visitor looking at completed task */ }
                        }
                    }

                    // 9. Share Section
                    item { ShareSection(t) }

                    // 10. Payment Notice
                    item { PaymentNotice(t.budget) }
                }
            }

            // Dialogs
            

            if (showRejectionDialog) {
                RejectionDialog(
                    onDismiss = { showRejectionDialog = false },
                    onSubmit = { reason ->
                        task?.let { viewModel.rejectCompletion(it._id, reason) }
                        showRejectionDialog = false
                    }
                )
            }

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Delete Task") },
                    text = { Text("Are you sure you want to delete this task?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                task?.let {
                                    viewModel.deleteTask(it._id) { success ->
                                        if (success) navController.popBackStack()
                                    }
                                }
                                showDeleteConfirmation = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
                    }
                )
            }
            
            if (showReviewDialog) {
                ReviewDialog(
                    onDismiss = { showReviewDialog = false },
                    onConfirm = { rating, comment ->
                        task?.let {
                            val revieweeId = if (currentUser?.id == it.posterId) it.claimedBy ?: "" else it.posterId
                            viewModel.submitReview(it._id, revieweeId, rating, comment)
                        }
                        showReviewDialog = false
                    }
                )
            }

            
        }
    }
}

@Composable
fun TaskInfoCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (task.urgent == 1) {
                    CategoryChip("⚡ URGENT", ErrorColor.copy(alpha = 0.12f), ErrorColor)
                }
                if (task.visibility == "private") {
                    CategoryChip("🔒 Private", SurfaceVariantColor, MutedText)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Description, null, tint = PrimaryDark, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(task.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryDark, lineHeight = 28.sp)
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, containerColor: Color, textColor: Color) {
    Surface(color = containerColor, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Text(label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun DescriptionSection(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📝 Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PrimaryDark)
            Spacer(Modifier.height(8.dp))
            Text(description, fontSize = 15.sp, color = Color(0xFF4D4D4D), lineHeight = 24.sp)
        }
    }
}

@Composable
fun InfoGrid(task: Task) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox("💰 Budget", "₹${task.budget}", Modifier.weight(1f))
        InfoBox("📅 Deadline", task.deadline, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox("📍 Location", task.location.split(",").firstOrNull() ?: task.location, Modifier.weight(1f))
        InfoBox("🕐 Posted", com.strangerhelp.app.utils.TimeUtils.getTimeAgo(task.createdAt), Modifier.weight(1f))
    }
}

@Composable
fun InfoBox(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 12.sp, color = MutedText, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryDark)
        }
    }
}

@Composable
fun GroupTaskProgress(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("👥 Group Task", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PrimaryDark)
                Surface(color = AccentOrange, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                    Text("0/${task.maxClaimers} Joined", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = 0f, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = AccentOrange, trackColor = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun MapSection(task: Task) {
    val lat = task.lat
    val lng = task.lng
    val location = task.location
    val context = LocalContext.current

    if (lat == null || lng == null) {
        // No location available
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceVariantColor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MutedText
                    )
                    Text(
                        text = "No location provided",
                        fontSize = 14.sp,
                        color = MutedText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            // Map Preview
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTag("task_map_${System.currentTimeMillis()}")
                        getMapAsync { map ->
                            try {
                                // Set style (free, no API key)
                                map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                                    // Position the camera
                                    val cameraPosition = CameraPosition.Builder()
                                        .target(LatLng(lat, lng))
                                        .zoom(14.0)
                                        .build()
                                    map.cameraPosition = cameraPosition

                                    // Add a marker
                                    val markerOptions = MarkerOptions()
                                        .position(LatLng(lat, lng))
                                        .title(location ?: "Task Location")
                                        .snippet("Tap for directions")
                                    map.addMarker(markerOptions)
                                }
                            } catch (e: Exception) {
                                // Handle map load errors
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // Location Address and Directions Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Location address
                Text(
                    text = "📍 ${location ?: "Unknown location"}",
                    fontSize = 12.sp,
                    color = PrimaryDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Get Directions Button
                OutlinedButton(
                    onClick = {
                        openGoogleMapsDirections(context, lat, lng, location)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentOrange
                    ),
                    border = BorderStroke(1.dp, AccentOrange)
                ) {
                    Icon(
                        Icons.Outlined.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccentOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get Directions (Google Maps)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PosterInfoCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceVariantColor),
                contentAlignment = Alignment.Center
            ) {
                Text(if (task.anonymous == 1) "?" else task.posterName.firstOrNull()?.uppercase() ?: "U", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (task.anonymous == 1) "Anonymous" else task.posterName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                    if (task.posterVerified && task.anonymous != 1) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = TrustColor, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("• ${com.strangerhelp.app.utils.TimeUtils.getTimeAgo(task.createdAt)}", fontSize = 13.sp, color = MutedText)
                }
            }
        }
    }
}

@Composable
fun ClaimRequestsSection(task: Task, viewModel: TaskDetailViewModel) {
    val pendingRequests = task.claimRequests?.filter { it.status == "pending" } ?: emptyList()
    if (pendingRequests.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🔔 Claim Requests (${pendingRequests.size})", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            pendingRequests.forEach { request ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(request.requesterName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Just now", fontSize = 11.sp, color = MutedText)
                        }
                        
                        request.offeredBudget?.let { offered ->
                            val diff = offered - task.budget
                            val msg = "Asking: ₹$offered " + if (diff > 0) "(+₹$diff)" else if (diff < 0) "(₹${-diff} less)" else "(same)"
                            val color = if (diff > 0) ErrorColor else if (diff < 0) TrustColor else MutedText
                            Text(msg, fontSize = 13.sp, color = color, modifier = Modifier.padding(top = 4.dp))
                        }
                        
                        request.message?.takeIf { it.isNotBlank() }?.let { msg ->
                            Text("\"$msg\"", fontSize = 13.sp, color = Color(0xFF4D4D4D), fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 4.dp))
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.approveClaim(task._id, request.requesterId) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanDeep),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) { Text("Approve", color = Color.White) }
                            
                            OutlinedButton(
                                onClick = { viewModel.rejectClaim(task._id, request.requesterId) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                                border = BorderStroke(1.dp, ErrorColor),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) { Text("Reject") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PosterActions(task: Task, viewModel: TaskDetailViewModel, navController: NavController, onDelete: () -> Unit, onReview: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (task.status == "open") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { /* Edit Task */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.dp, PrimaryDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit")
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.dp, ErrorColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        }
        
        if (task.status == "claimed") {
            Button(
                onClick = { 
                    val claimerId = task.claimedBy
                    if (claimerId != null) {
                        viewModel.messagePoster(task._id, claimerId) { convId ->
                            navController.navigate("chat/$convId")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("💬 Chat with Helper")
            }
            
            if (task.completionStatus == "pending") {
                Card(colors = CardDefaults.cardColors(containerColor = CyanDeep.copy(alpha = 0.1f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Proof Submitted", fontWeight = FontWeight.Bold, color = CyanDeep)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.rejectCompletion(task._id, "Needs revision") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor)) { Text("Reject") }
                            Button(onClick = { viewModel.acceptCompletion(task._id) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = TrustColor)) { Text("Accept") }
                        }
                    }
                }
            }
        }
        
        if (task.status == "completed") {
            Button(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                Text("Leave a Review")
            }
        }
    }
}

@Composable
fun HelperActions(task: Task, viewModel: TaskDetailViewModel, navController: NavController, onSubmitProof: () -> Unit, onReview: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (task.completionStatus) {
            "", null -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TrustColor.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "You are the helper for this task!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrustColor
                        )
                    }
                }

                Button(
                    onClick = onSubmitProof,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("📸 Submit Proof")
                }
                
                OutlinedButton(
                    onClick = { viewModel.startTracking(task._id) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, PrimaryDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                ) {
                    Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("🗺️ Start Tracking")
                }
                
                OutlinedButton(
                    onClick = { 
                        viewModel.messagePoster(task._id, task.posterId) { convId ->
                            navController.navigate("chat/$convId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, PrimaryDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("💬 Chat with Poster")
                }
            }
            "pending" -> {
                Card(colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Proof submitted — waiting for poster to review", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)
                    }
                }
            }
            "rejected" -> {
                Card(colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("❌ Proof Rejected", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ErrorColor)
                        Spacer(Modifier.height(4.dp))
                        Text("Please resubmit", fontSize = 13.sp, color = ErrorColor)
                    }
                }
                Button(onClick = onSubmitProof, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                    Text("📸 Resubmit Proof")
                }
            }
            "accepted" -> {
                Card(colors = CardDefaults.cardColors(containerColor = TrustColor.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Task completed!", fontSize = 16.sp, color = TrustColor, fontWeight = FontWeight.Bold)
                    }
                }
                Button(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                    Text("⭐ Leave a Review")
                }
            }
        }
    }
}

@Composable
fun VisitorActions(
    task: Task,
    viewModel: TaskDetailViewModel,
    navController: NavController,
    taskId: String
) {
    val claimState by viewModel.claimState.collectAsStateWithLifecycle()
    val isClaiming by viewModel.isClaiming.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        // ⭐ P2P Payment Notice
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = BorderStroke(1.dp, Color(0xFFFFB300)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFF8F00), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("⚠️ Direct Payment — Escrow Coming Soon", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                    Text("Payment of ₹${task.budget} is made directly by the poster to the helper via UPI after completion. StrangerHelp does not hold or process payments.", fontSize = 12.sp, color = Color(0xFFE65100), lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        
        ClaimButton(
            state = claimState,
            taskBudget = task.budget,
            maxClaimers = task.maxClaimers,
            claimedUsers = task.claimedUsers,
            onClaimClick = { showDialog = true }
        )

        if (showDialog) {
            ClaimDialog(
                taskBudget = task.budget,
                isGroupTask = task.maxClaimers > 1,
                onDismiss = { showDialog = false },
                onSubmit = { budget, message ->
                    showDialog = false
                    viewModel.requestToClaim(
                        taskId = taskId,
                        offeredBudget = budget,
                        message = message,
                        onSuccess = { conversationId ->
                            navController.navigate("chat/$conversationId") {
                                popUpTo("task/${taskId}") { inclusive = true }
                            }
                        }
                    )
                },
                isLoading = isClaiming
            )
        }

        OutlinedButton(
            onClick = {
                viewModel.messagePoster(taskId, task.posterId) { conversationId ->
                    navController.navigate("chat/$conversationId")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark),
            border = BorderStroke(1.dp, PrimaryDark)
        ) {
            Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("💬 Message Poster", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LoginPrompt(navController: NavController) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceVariantColor), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Login to claim this task or message the poster.", fontSize = 14.sp, color = MutedText, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { navController.navigate("login") }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)) {
                Text("Login / Register")
            }
        }
    }
}

@Composable
fun ShareSection(task: Task) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { shareTask(context, task) },
            modifier = Modifier.weight(1f).height(48.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
        ) {
            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Share Task")
        }
        
        Button(
            onClick = { shareToWhatsApp(context, task) },
            modifier = Modifier.weight(1f).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Text("📱 WhatsApp", color = Color.White)
        }
    }
}

@Composable
fun PaymentNotice(budget: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            Text("⚠️", fontSize = 20.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Direct Payment — Escrow Coming Soon", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AccentOrange)
                Spacer(Modifier.height(4.dp))
                Text("Payment of ₹$budget is made directly by the poster to the claimer. Verify payment terms via messages before starting.", fontSize = 12.sp, color = Color(0xFF4D4D4D), lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun ClaimDialog(
    taskBudget: Int,
    isGroupTask: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int?, String?) -> Unit,
    isLoading: Boolean
) {
    var offeredBudget by remember { mutableStateOf<String>("") }
    var message by remember { mutableStateOf("") }
    var showBudget by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = {
            keyboardController?.hide()
            onDismiss()
        },
        containerColor = Color.White,
        title = {
            Text(if (isGroupTask) "Request to Join" else "Request to Claim", fontWeight = FontWeight.Bold, color = PrimaryDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (isGroupTask) {
                        "You're requesting to join this group task. The poster will need to approve your request."
                    } else {
                        "You're requesting to claim this task. The poster will need to approve your request."
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF4D4D4D)
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showBudget = !showBudget }) {
                    Checkbox(
                        checked = showBudget,
                        onCheckedChange = { showBudget = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentOrange)
                    )
                    Text("Suggest a different budget", fontSize = 14.sp, color = PrimaryDark)
                }

                if (showBudget) {
                    Column {
                        OutlinedTextField(
                            value = offeredBudget,
                            onValueChange = { offeredBudget = it },
                            placeholder = { Text("Enter amount (₹)", color = MutedText) },
                            leadingIcon = { Text("₹", color = MutedText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Original budget: ₹$taskBudget",
                            fontSize = 10.sp,
                            color = MutedText
                        )
                    }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Add a message (optional)", color = MutedText) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CyanDeep.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "You'll be redirected to chat with the poster after sending your request.",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    keyboardController?.hide()
                    onSubmit(if (showBudget) offeredBudget.toIntOrNull() else null, message.takeIf { it.isNotBlank() })
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Request")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    keyboardController?.hide()
                    onDismiss()
                }, 
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryDark)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RejectionDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    AlertDialog(
        onDismissRequest = {
            keyboardController?.hide()
            onDismiss()
        },
        title = { Text("Reject Proof") },
        text = {
            OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason for Rejection") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        },
        confirmButton = { Button(onClick = { keyboardController?.hide(); onSubmit(reason) }, colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)) { Text("Reject") } },
        dismissButton = { TextButton(onClick = { keyboardController?.hide(); onDismiss() }) { Text("Cancel") } }
    )
}

@Composable
fun ReviewDialog(onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    AlertDialog(
        onDismissRequest = {
            keyboardController?.hide()
            onDismiss()
        },
        title = { Text("Leave a Review") },
        text = {
            Column {
                Text("Rating: $rating / 5", fontWeight = FontWeight.Bold)
                Slider(value = rating.toFloat(), onValueChange = { rating = it.toInt() }, valueRange = 1f..5f, steps = 3, colors = SliderDefaults.colors(thumbColor = AccentOrange, activeTrackColor = AccentOrange))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Comment") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = { Button(onClick = { keyboardController?.hide(); onConfirm(rating, comment) }, colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) { Text("Submit Review") } },
        dismissButton = { TextButton(onClick = { keyboardController?.hide(); onDismiss() }) { Text("Cancel") } }
    )
}

fun shareTask(context: Context, task: Task) {
    val shareText = """
        🆘 Task: ${task.title}
        💰 Earn ₹${task.budget}
        📍 ${task.location}
        
        Complete this task nearby and earn money on StrangerHelp!
        https://strangerhelp.com/tasks/${task._id}
    """.trimIndent()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "₹${task.budget} — ${task.title}")
    }
    context.startActivity(Intent.createChooser(intent, "Share Task"))
}

fun shareToWhatsApp(context: Context, task: Task) {
    val shareText = "Earn ₹${task.budget} on StrangerHelp! Check out this task: ${task.title}\nhttps://strangerhelp.com/tasks/${task._id}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        shareTask(context, task)
    }
}

fun openGoogleMapsDirections(
    context: Context,
    lat: Double,
    lng: Double,
    destination: String? = null
) {
    try {
        val uri = Uri.Builder()
            .scheme("https")
            .authority("www.google.com")
            .path("/maps/dir/")
            .appendQueryParameter("api", "1")
            .appendQueryParameter("destination", "$lat,$lng")
            .appendQueryParameter("travelmode", "driving")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        val uri = Uri.parse("https://www.google.com/maps?q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}


class TaskDetailViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return TaskDetailViewModel() as T
    }
}


@Composable
fun TaskLifecycleStepper(task: Task, currentUserId: String?) {
    val isOwner = currentUserId == task.posterId
    val isClaimer = currentUserId == task.claimedBy
    
    // Determine the current step index (0 to 3)
    val currentStep = when {
        task.status == "completed" -> 3
        task.completionStatus == "pending" -> 2
        task.status == "claimed" || task.status == "in_progress" -> 1
        else -> 0
    }
    
    // Define the step labels based on perspective
    val steps = if (isOwner) {
        listOf("Open", "In Progress", "Review", "Completed")
    } else if (isClaimer) {
        listOf("Claimed", "Working", "Submitted", "Completed")
    } else {
        listOf("Open", "Claimed", "Verifying", "Completed")
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Task Lifecycle",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PrimaryDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, label ->
                    val isCompleted = index < currentStep
                    val isCurrent = index == currentStep
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> TrustColor
                                        isCurrent -> AccentOrange
                                        else -> SurfaceVariantColor
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else if (isCurrent) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Label
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent || isCompleted) PrimaryDark else MutedText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    
                    // Line separator (except for last item)
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .height(2.dp)
                                .background(if (isCompleted) TrustColor else SurfaceVariantColor)
                        )
                    }
                }
            }
            
            // Helpful description of current state
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusText = when {
                    isOwner && currentStep == 0 -> "Task is open. Waiting for someone to request to claim it."
                    isOwner && currentStep == 1 -> "A helper is currently working on this task."
                    isOwner && currentStep == 2 -> "The helper has submitted proof. Please review and accept/reject it."
                    isOwner && currentStep == 3 -> "Task is completed and verified. Don't forget to leave a review!"
                    
                    isClaimer && currentStep == 0 -> "Your request was approved. You can now start working."
                    isClaimer && currentStep == 1 -> "You are actively working on this task. Submit proof when done."
                    isClaimer && currentStep == 2 -> "Proof submitted! Waiting for the poster to verify it."
                    isClaimer && currentStep == 3 -> "Great job! Task is fully completed."
                    
                    currentStep == 0 -> "This task is open and available to claim."
                    currentStep == 1 -> "Someone is currently working on this task."
                    currentStep == 2 -> "This task is currently being verified."
                    currentStep == 3 -> "This task has been successfully completed."
                    else -> ""
                }
                
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(statusText, fontSize = 12.sp, color = Color(0xFF4D4D4D))
                }
            }
        }
    }
}
