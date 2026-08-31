import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

target = """            TaskUiState.VISITOR_MUST_LOGIN -> {
                LoginPrompt(navController)
            }"""

replacement = """            TaskUiState.POSTER_WAITING -> {
                Card(colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Waiting for helpers to request your task.", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)
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
                        onReject = { viewModel.rejectClaim(task._id, request.requesterId) }
                    )
                }
            }
            TaskUiState.POSTER_CLAIMED_WAITING -> {
                Card(colors = CardDefaults.cardColors(containerColor = TrustColor.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Claimed by ${task.claimedByName ?: "Helper"}", fontSize = 16.sp, color = TrustColor, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                LiveTrackingSection(task = task)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { 
                        viewModel.messagePoster(task._id, task.claimedBy ?: "") { convId ->
                            navController.navigate("chat/$convId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, PrimaryDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                ) {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("💬 Message Helper")
                }
            }
            TaskUiState.POSTER_REVIEW_PROOF -> {
                ProofReviewSection(
                    task = task,
                    onAccept = { viewModel.acceptCompletion(task._id) },
                    onReject = { reason -> viewModel.rejectCompletion(task._id, reason) }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { 
                        viewModel.messagePoster(task._id, task.claimedBy ?: "") { convId ->
                            navController.navigate("chat/$convId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, PrimaryDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                ) {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("💬 Message Helper")
                }
            }
            TaskUiState.POSTER_PROOF_REJECTED -> {
                Card(colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ Proof Rejected", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ErrorColor)
                        Spacer(Modifier.height(4.dp))
                        Text("Waiting for helper to resubmit.", fontSize = 13.sp, color = ErrorColor)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { 
                        viewModel.messagePoster(task._id, task.claimedBy ?: "") { convId ->
                            navController.navigate("chat/$convId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, PrimaryDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                ) {
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("💬 Message Helper")
                }
            }
            TaskUiState.POSTER_COMPLETED -> {
                Card(colors = CardDefaults.cardColors(containerColor = TrustColor.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Task completed!", fontSize = 16.sp, color = TrustColor, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                PosterReviewSection(
                    task = task,
                    onReviewSubmit = { rating, comment ->
                        // Needs viewModel.submitReview
                    }
                )
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
            TaskUiState.VISITOR_MUST_LOGIN -> {
                LoginPrompt(navController)
            }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)
