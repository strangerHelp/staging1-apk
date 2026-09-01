package com.strangerhelp.app.ui.screens.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val reviewViewModel: ReviewViewModel = viewModel { ReviewViewModel() }
    val task by viewModel.task.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isClaiming by viewModel.isClaiming.collectAsStateWithLifecycle()
    val isSubmittingProof by viewModel.isSubmittingProof.collectAsStateWithLifecycle()
    val context = LocalContext.current
    

    
    var showRejectionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
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
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { task?.let { shareTask(navController.context, it) } }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    if (currentUser?.id == task?.posterId && task?.status == "open") {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "More Options")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Task") },
                                    onClick = {
                                        showMenu = false
                                        showEditDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Task", color = androidx.compose.ui.graphics.Color(0xFFEE0000)) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmation = true
                                    }
                                )
                            }
                        }
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
                Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFFAFAFA))) {
                    AnimatedVisibility(visible = isSyncing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    AnimatedVisibility(visible = isOffline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reconnecting... showing stale data",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                    // 1. Task Info Card
                    item { TaskInfoCard(t) }

                    // 2. Description
                    if (t.description.isNotEmpty()) {
                        item { DescriptionSection(t.description) }
                    }
                    
                    // 2.5 Attachments
                    if (t.attachments.isNotEmpty()) {
                        item { com.strangerhelp.app.ui.components.AttachmentsSection(t.attachments) }
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
                    // ClaimRequestsSection removed since it is now in TaskActionSection

                    // 8. Action Buttons
                    item {
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle(initialValue = TaskUiState.VISITOR_CAN_REQUEST)
                        
                        TaskActionSection(
                            uiState = uiState,
                            task = t,
                            viewModel = viewModel,
                            navController = navController,
                            onDelete = { showDeleteConfirmation = true },
                            onEdit = { showEditDialog = true },
                            onReview = { showReviewDialog = true }
                        )
                    }

                    // 9. Share Section
                    item { ShareSection(t) }

                    // 10. Payment Notice
                    item { PaymentNotice(t.budget) }

                    if (t.status == "completed") {
                        val isParticipant = currentUser?.id == t.posterId || currentUser?.id == t.claimedBy

                        if (isParticipant && currentUser != null) {
                            item {
                                RatingComponent(
                                    task = t,
                                    viewModel = reviewViewModel
                                )
                            }
                        }

                        item {
                            val reviews by reviewViewModel.taskReviews.collectAsStateWithLifecycle()
                            LaunchedEffect(Unit) {
                                reviewViewModel.loadTaskReviews(t._id)
                            }

                            if (reviews?.reviews?.isNotEmpty() == true) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "📋 Existing Reviews (${reviews?.totalReviews ?: 0})",
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    ReviewList(reviews = reviews?.reviews ?: emptyList())
                                }
                            }
                        }
                    }
                }
            }
        }

            // Dialogs
            

            if (showEditDialog && task != null) {
                EditTaskDialog(
                    task = task!!,
                    onDismiss = { showEditDialog = false },
                    onSave = { updates ->
                        viewModel.editTask(task!!._id, updates) { success ->
                            if (success) {
                                showEditDialog = false
                            } else {
                                Toast.makeText(context, "Failed to edit task", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

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
                            colors = ButtonDefaults.textButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFEE0000))
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
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
                    CategoryChip("⚡ URGENT", androidx.compose.ui.graphics.Color(0xFFEE0000).copy(alpha = 0.12f), androidx.compose.ui.graphics.Color(0xFFEE0000))
                }
                if (task.visibility == "private") {
                    CategoryChip("🔒 Private", androidx.compose.ui.graphics.Color(0xFFF5F5F5), androidx.compose.ui.graphics.Color(0xFF666666))
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
            Text(title, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF666666), fontWeight = FontWeight.Medium)
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
                containerColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5)
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
                        tint = androidx.compose.ui.graphics.Color(0xFF666666)
                    )
                    Text(
                        text = "No location provided",
                        fontSize = 14.sp,
                        color = androidx.compose.ui.graphics.Color(0xFF666666),
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

                                    // Add a marker for the task
                                    val markerOptions = MarkerOptions()
                                        .position(LatLng(lat, lng))
                                        .title(location ?: "Task Location")
                                        .snippet("Tap for directions")
                                    map.addMarker(markerOptions)

                                    if (task.trackingActive && task.helperLat != null && task.helperLng != null) {
                                        val helperMarker = MarkerOptions()
                                            .position(LatLng(task.helperLat, task.helperLng))
                                            .title("Helper's Location")
                                            .snippet("Live tracking")
                                        map.addMarker(helperMarker)
                                    }
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
                modifier = Modifier.size(48.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFFF5F5F5)),
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
                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = androidx.compose.ui.graphics.Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("• ${com.strangerhelp.app.utils.TimeUtils.getTimeAgo(task.createdAt)}", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFF666666))
                }
            }
        }
    }
}

