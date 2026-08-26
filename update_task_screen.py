import re

code = """
package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.repository.AuthRepository
import com.strangerhelp.app.data.repository.TaskRepository

val Primary = Color(0xFFF59E0B)
val Muted = Color(0xFF757575)
val Warning = Color(0xFFF57C00)
val Success = Color(0xFF388E3C)
val Error = Color(0xFFD32F2F)

sealed class HelperState {
    object CAN_CLAIM : HelperState()
    object REQUEST_SENT : HelperState()
    object REQUEST_REJECTED : HelperState()
    object SUBMIT_PROOF : HelperState()
    object AWAITING_REVIEW : HelperState()
    object RESUBMIT_PROOF : HelperState()
    object COMPLETED : HelperState()
    object NOT_INVOLVED : HelperState()
}

sealed class PosterState {
    object WAITING_FOR_HELPERS : PosterState()
    object REVIEW_REQUESTS : PosterState()
    object HELPER_WORKING : PosterState()
    object REVIEW_PROOF : PosterState()
    object WAITING_RESUBMIT : PosterState()
    object COMPLETED : PosterState()
}

fun getHelperState(task: Task, currentUserId: String): HelperState {
    return when {
        task.status == "open" -> {
            val myRequest = task.claimRequests?.find { it.requesterId == currentUserId }
            when (myRequest?.status) {
                null -> HelperState.CAN_CLAIM
                "pending" -> HelperState.REQUEST_SENT
                "rejected" -> HelperState.REQUEST_REJECTED
                else -> HelperState.CAN_CLAIM
            }
        }
        task.status == "claimed" && task.claimedBy == currentUserId -> {
            when (task.completionStatus) {
                "", null -> HelperState.SUBMIT_PROOF
                "pending" -> HelperState.AWAITING_REVIEW
                "rejected" -> HelperState.RESUBMIT_PROOF
                "accepted" -> HelperState.COMPLETED
                else -> HelperState.SUBMIT_PROOF
            }
        }
        task.status == "completed" && task.claimedBy == currentUserId -> HelperState.COMPLETED
        else -> HelperState.NOT_INVOLVED
    }
}

fun getPosterState(task: Task): PosterState {
    return when {
        task.status == "open" && task.claimRequests?.any { it.status == "pending" } == true ->
            PosterState.REVIEW_REQUESTS
        task.status == "open" -> PosterState.WAITING_FOR_HELPERS
        task.status == "claimed" -> {
            when (task.completionStatus) {
                "", null -> PosterState.HELPER_WORKING
                "pending" -> PosterState.REVIEW_PROOF
                "rejected" -> PosterState.WAITING_RESUBMIT
                "accepted" -> PosterState.COMPLETED
                else -> PosterState.HELPER_WORKING
            }
        }
        task.status == "completed" -> PosterState.COMPLETED
        else -> PosterState.WAITING_FOR_HELPERS
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    taskId: String,
    viewModel: TaskDetailViewModel = viewModel(factory = TaskDetailViewModelFactory())
) {
    val task by viewModel.task.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showClaimDialog by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task?.title ?: "Task Details", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && task == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (task != null && currentUser != null) {
            val isPoster = task!!.posterId == currentUser!!.id
            val helperState = if (!isPoster) getHelperState(task!!, currentUser!!.id) else null
            val posterState = if (isPoster) getPosterState(task!!) else null

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFFAFAFA)),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Task Info
                item { TaskInfoSection(task!!) }

                // Actions based on Actor
                item {
                    if (isPoster) {
                        PosterActionsSection(task!!, posterState!!, viewModel)
                    } else {
                        HelperActionsSection(task!!, helperState!!, onClaimClick = { showClaimDialog = true }, viewModel)
                    }
                }
            }
            
            if (showClaimDialog) {
                ClaimDialog(
                    task = task!!,
                    onDismiss = { showClaimDialog = false },
                    onConfirm = { budget, msg ->
                        viewModel.claimTask(task!!._id, budget, msg)
                        showClaimDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun TaskInfoSection(task: Task) {
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = task.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Warning
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(task.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text("₹${task.budget}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn, "Location", tint = Muted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(task.location, fontSize = 14.sp, color = Muted)
        }
        
        Spacer(Modifier.height(24.dp))
        Text("Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(task.description, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF333333))
    }
}

@Composable
fun PosterActionsSection(task: Task, state: PosterState, viewModel: TaskDetailViewModel) {
    Column(Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Status", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))
        
        when (state) {
            is PosterState.WAITING_FOR_HELPERS -> {
                StatusCard(
                    icon = Icons.Outlined.HourglassEmpty,
                    title = "Waiting for Helpers",
                    subtitle = "Your task is open. We'll notify you when someone wants to help.",
                    color = Muted
                )
            }
            is PosterState.REVIEW_REQUESTS -> {
                Text("Claim Requests (${task.claimRequests?.filter { it.status == "pending" }?.size ?: 0})", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                task.claimRequests?.filter { it.status == "pending" }?.forEach { req ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(req.requesterName, fontWeight = FontWeight.Bold)
                            Text("Offered: ₹${req.offeredBudget ?: task.budget}", color = Primary, fontWeight = FontWeight.Medium)
                            if (!req.message.isNullOrEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(req.message, fontSize = 14.sp, color = Muted)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { viewModel.rejectClaim(task._id, req.requesterId) }, modifier = Modifier.weight(1f)) {
                                    Text("Decline", color = Error)
                                }
                                Button(onClick = { viewModel.approveClaim(task._id, req.requesterId) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                                    Text("Approve")
                                }
                            }
                        }
                    }
                }
            }
            is PosterState.HELPER_WORKING -> {
                StatusCard(
                    icon = Icons.Outlined.DirectionsWalk,
                    title = "Helper Assigned",
                    subtitle = "${task.claimedByName ?: "Helper"} is working on this task.",
                    color = Primary
                )
            }
            is PosterState.REVIEW_PROOF -> {
                StatusCard(
                    icon = Icons.Outlined.CheckCircle,
                    title = "Proof Submitted",
                    subtitle = "Review the completion proof.",
                    color = Primary
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.rejectCompletion(task._id, "Incomplete work") }, modifier = Modifier.weight(1f)) {
                        Text("Reject", color = Error)
                    }
                    Button(onClick = { viewModel.acceptCompletion(task._id) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Success)) {
                        Text("Accept")
                    }
                }
            }
            is PosterState.WAITING_RESUBMIT -> {
                StatusCard(
                    icon = Icons.Outlined.ErrorOutline,
                    title = "Waiting for Resubmission",
                    subtitle = "You rejected the proof. Waiting for helper to resubmit.",
                    color = Warning
                )
            }
            is PosterState.COMPLETED -> {
                StatusCard(
                    icon = Icons.Filled.CheckCircle,
                    title = "Task Completed!",
                    subtitle = "You can now leave a review.",
                    color = Success
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { /* TODO Review */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Leave a Review")
                }
            }
        }
    }
}

@Composable
fun HelperActionsSection(task: Task, state: HelperState, onClaimClick: () -> Unit, viewModel: TaskDetailViewModel) {
    Column(Modifier.padding(16.dp).fillMaxWidth()) {
        when (state) {
            is HelperState.CAN_CLAIM -> {
                Button(
                    onClick = onClaimClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("💰 Claim This Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            is HelperState.REQUEST_SENT -> {
                StatusCard(Icons.Outlined.HourglassTop, "Request Sent", "Waiting for poster to approve your request.", Muted)
            }
            is HelperState.REQUEST_REJECTED -> {
                StatusCard(Icons.Outlined.Cancel, "Request Rejected", "The poster declined your request.", Error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onClaimClick, modifier = Modifier.fillMaxWidth()) { Text("🔄 Try Again") }
            }
            is HelperState.SUBMIT_PROOF -> {
                StatusCard(Icons.Outlined.CameraAlt, "Submit Proof", "Take a photo of the completed task to get paid.", Primary)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.submitProof(task._id, emptyList()) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("📸 Submit Proof")
                }
            }
            is HelperState.AWAITING_REVIEW -> {
                StatusCard(Icons.Outlined.RateReview, "Proof Submitted", "Awaiting poster's review.", Primary)
            }
            is HelperState.RESUBMIT_PROOF -> {
                StatusCard(Icons.Outlined.Error, "Proof Rejected", "Please resubmit proof of completion.", Error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.submitProof(task._id, emptyList()) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("📸 Resubmit Proof")
                }
            }
            is HelperState.COMPLETED -> {
                StatusCard(Icons.Filled.CheckCircle, "Task Completed!", "Great job!", Success)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { /* TODO Review */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("⭐ Leave a Review")
                }
            }
            is HelperState.NOT_INVOLVED -> {
                StatusCard(Icons.Outlined.Info, "Not Available", "This task is no longer available.", Muted)
            }
        }
    }
}

@Composable
fun StatusCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, color: Color) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
                Text(subtitle, color = Muted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ClaimDialog(task: Task, onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var budget by remember { mutableStateOf(task.budget.toString()) }
    var message by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Claim Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Your Offer (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(budget.toIntOrNull() ?: task.budget, message) }) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

class TaskDetailViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return TaskDetailViewModel() as T
    }
}
"""

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(code)

