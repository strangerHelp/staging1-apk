package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.ui.theme.*

@Composable
fun TaskActionSection(
    uiState: TaskUiState,
    task: Task,
    viewModel: TaskDetailViewModel,
    navController: NavController,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onReview: () -> Unit
) {
    val isProcessing by viewModel.isProcessingClaim.collectAsState()
    var showClaimDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        
        // Show P2P Payment Notice for non-owners looking at open tasks
        if (uiState in listOf(TaskUiState.HELPER_CAN_REQUEST, TaskUiState.VISITOR_CAN_REQUEST, TaskUiState.VISITOR_MUST_LOGIN)) {
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
        }

        when (uiState) {


            TaskUiState.HELPER_CAN_REQUEST -> {
                Button(
                    onClick = { showClaimDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text(if (task.maxClaimers > 1) "Request to Join Group" else "Request to Claim Task")
                }
                ChatWithPosterButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    label = "Message Poster"
                )
            }
            TaskUiState.HELPER_REQUEST_PENDING -> {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.LightGray, disabledContentColor = Color.White)
                ) {
                    Text("⏳ Request Sent — Waiting for Approval")
                }
                Spacer(Modifier.height(8.dp))
                ChatWithPosterButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController,
                    label = "💬 Chat with Poster"
                )
            }
            TaskUiState.HELPER_REQUEST_REJECTED -> {
                Button(
                    onClick = { showClaimDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Request Rejected — Request Again")
                }
                Spacer(Modifier.height(8.dp))
                ChatWithPosterButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController,
                    label = "💬 Chat with Poster"
                )
            }
            TaskUiState.HELPER_CLAIMED_CAN_TRACK, TaskUiState.HELPER_TRACKING -> {
                val isTracking = uiState == TaskUiState.HELPER_TRACKING
                
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (task.maxClaimers > 1) "You joined this group task!" else "You are the helper for this task!", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = androidx.compose.ui.graphics.Color(0xFF10B981))
                    }
                }

                Button(
                    onClick = { navController.navigate("gps_camera/${task._id}") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("📸 Take Photo & Submit Proof")
                }

                HelperTrackingControls(task = task, viewModel = viewModel)
                
                ChatWithPosterButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController,
                    label = "💬 Chat with Poster"
                )
            }
            TaskUiState.HELPER_PROOF_PENDING -> {
                Card(colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Proof submitted — waiting for poster to review", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                ChatWithPosterButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController,
                    label = "💬 Chat with Poster"
                )
                Spacer(Modifier.height(16.dp))
                ProofGallery(proof = task.completionProof, canReview = false)
            }
            TaskUiState.HELPER_PROOF_REJECTED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEE0000).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("❌ Proof Rejected", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                        Spacer(Modifier.height(4.dp))
                        if (!task.rejectionReason.isNullOrBlank()) {
                            Text("Reason: ${task.rejectionReason}", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                            Spacer(Modifier.height(4.dp))
                        }
                        Text("Please resubmit", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("gps_camera/${task._id}") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("📸 Retake Photo & Resubmit")
                }
                Spacer(Modifier.height(8.dp))
                ChatWithPosterButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController,
                    label = "💬 Chat with Poster"
                )
                Spacer(Modifier.height(16.dp))
                ProofGallery(proof = task.completionProof, canReview = false)
            }
            TaskUiState.HELPER_COMPLETED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Task completed!", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                ChatWithPosterButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController,
                    label = "💬 Chat with Poster"
                )
                Spacer(Modifier.height(8.dp))

                ProofGallery(proof = task.completionProof, canReview = false)
            }
            TaskUiState.POSTER_WAITING -> {
                Card(colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Waiting for helpers to request your task.", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onEdit,
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
                        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFEE0000)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFEE0000))
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
            TaskUiState.POSTER_HAS_REQUESTS -> {
                Text("🔔 Claim Requests (${task.claimRequests?.count { it.status == "pending" } ?: 0})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                task.claimRequests?.filter { it.status == "pending" }?.forEach { request ->
                    ClaimRequestCard(
                        request = request,
                        taskBudget = task.budget,
                        onApprove = { viewModel.approveClaim(task._id, request.requesterId) },
                        onReject = { viewModel.rejectClaim(task._id, request.requesterId) },
                        isProcessing = isProcessing
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onEdit,
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
                        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFEE0000)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFEE0000))
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
            TaskUiState.POSTER_CLAIMED_WAITING -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Claimed by ${task.claimedByName ?: "Helper"}", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                LiveTrackingSection(task = task)
                Spacer(Modifier.height(8.dp))
                ChatWithMessageHelperButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            TaskUiState.POSTER_REVIEW_PROOF -> {
                ProofGallery(
                    proof = task.completionProof,
                    canReview = true,
                    onAccept = { viewModel.acceptCompletion(task._id) },
                    onReject = { reason -> viewModel.rejectCompletion(task._id, reason) }
                )
                Spacer(Modifier.height(8.dp))
                ChatWithMessageHelperButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            TaskUiState.POSTER_PROOF_REJECTED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEE0000).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ Proof Rejected", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                        Spacer(Modifier.height(4.dp))
                        if (!task.rejectionReason.isNullOrBlank()) {
                            Text("Reason given: ${task.rejectionReason}", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                            Spacer(Modifier.height(4.dp))
                        }
                        Text("Waiting for helper to resubmit.", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                    }
                }
                Spacer(Modifier.height(8.dp))
                ChatWithMessageHelperButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            TaskUiState.POSTER_COMPLETED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Task completed!", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                ChatWithMessageHelperButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController
                )
                Spacer(Modifier.height(16.dp))
                ProofGallery(proof = task.completionProof, canReview = false)
                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    border = BorderStroke(1.dp, Color(0xFFFFE69C))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ Direct Payment", fontWeight = FontWeight.Bold, color = Color(0xFF664D03))
                        Spacer(Modifier.height(4.dp))
                        Text("Please pay ₹${task.budget} to ${task.claimedByName ?: "the helper"} directly via UPI. Verify payment terms via messages.", fontSize = 13.sp, color = Color(0xFF664D03))
                    }
                }
            }
            TaskUiState.CLAIMED_VIEWER -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Task Claimed",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "This task is currently claimed by ${task.claimedByName ?: "a helper"} and is in progress.",
                                fontSize = 13.sp,
                                color = Muted
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                ChatWithPosterButton(
                    task = task,
                    viewModel = viewModel,
                    navController = navController,
                    label = "💬 Chat with Poster"
                )
            }
            TaskUiState.VISITOR_MUST_LOGIN -> {
                LoginPrompt(navController)
            }
            
            else -> {}
        }
    }

    if (showClaimDialog) {
        val isClaiming = viewModel.isClaiming.collectAsState().value
        ClaimDialog(
            taskBudget = task.budget,
            isGroupTask = task.maxClaimers > 1,
            onDismiss = { showClaimDialog = false },
            onSubmit = { budget, message ->
                showClaimDialog = false
                viewModel.requestToClaim(
                    taskId = task._id,
                    offeredBudget = budget,
                    message = message,
                    onSuccess = { conversationId ->
                        navController.navigate("chat/$conversationId") {
                            popUpTo("task/${task._id}") { inclusive = true }
                        }
                    }
                )
            },
            isLoading = isClaiming
        )
    }
}

@Composable
fun ChatWithPosterButton(
    task: Task,
    viewModel: TaskDetailViewModel,
    navController: NavController,
    modifier: Modifier = Modifier.fillMaxWidth().height(52.dp),
    label: String = "💬 Chat with Poster"
) {
    var isOpeningChat by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { 
            if (!isOpeningChat) {
                isOpeningChat = true
                viewModel.messagePoster(
                    taskId = task._id,
                    posterId = task.posterId,
                    onError = {
                        isOpeningChat = false
                    },
                    onResult = { convId ->
                        isOpeningChat = false
                        if (convId.isNotBlank()) {
                            try {
                                navController.navigate("chat/$convId")
                            } catch (e: Exception) {
                                try {
                                    navController.navigate("messages/$convId")
                                } catch (_: Exception) {}
                            }
                        }
                    }
                )
            }
        },
        enabled = !isOpeningChat,
        modifier = modifier,
        border = BorderStroke(1.dp, PrimaryDark),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
    ) {
        if (isOpeningChat) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryDark, strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text("Opening chat...")
        } else {
            Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    }
}

@Composable
fun ChatWithMessageHelperButton(
    task: Task,
    viewModel: TaskDetailViewModel,
    navController: NavController,
    modifier: Modifier = Modifier.fillMaxWidth().height(52.dp),
    label: String = "💬 Message Helper"
) {
    var isOpeningChat by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { 
            if (!isOpeningChat) {
                isOpeningChat = true
                viewModel.messagePoster(
                    taskId = task._id,
                    posterId = task.claimedBy ?: "",
                    onError = {
                        isOpeningChat = false
                    },
                    onResult = { convId ->
                        isOpeningChat = false
                        if (convId.isNotBlank()) {
                            try {
                                navController.navigate("chat/$convId")
                            } catch (e: Exception) {
                                try {
                                    navController.navigate("messages/$convId")
                                } catch (_: Exception) {}
                            }
                        }
                    }
                )
            }
        },
        enabled = !isOpeningChat,
        modifier = modifier,
        border = BorderStroke(1.dp, PrimaryDark),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
    ) {
        if (isOpeningChat) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryDark, strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text("Opening chat...")
        } else {
            Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    }
}
