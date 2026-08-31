import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

# Replace HELPER_PROOF_PENDING ProofDisplayComponent
content = content.replace(
    "ProofDisplayComponent(task.completionProof)",
    "ProofGallery(proof = task.completionProof, canReview = false)"
)

# HELPER_PROOF_REJECTED update
helper_rejected_old = """            TaskUiState.HELPER_PROOF_REJECTED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEE0000).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("❌ Proof Rejected", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                        Spacer(Modifier.height(4.dp))
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
                Spacer(Modifier.height(16.dp))
                ProofGallery(proof = task.completionProof, canReview = false)
            }"""

helper_rejected_new = """            TaskUiState.HELPER_PROOF_REJECTED -> {
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
                Spacer(Modifier.height(16.dp))
                ProofGallery(proof = task.completionProof, canReview = false)
            }"""

content = content.replace(helper_rejected_old, helper_rejected_new)

# POSTER_REVIEW_PROOF update
poster_review_old = """            TaskUiState.POSTER_REVIEW_PROOF -> {
                ProofReviewSection(
                    task = task,
                    onAccept = { viewModel.acceptCompletion(task._id) },
                    onReject = { reason -> viewModel.rejectCompletion(task._id, reason) }
                )"""
poster_review_new = """            TaskUiState.POSTER_REVIEW_PROOF -> {
                ProofGallery(
                    proof = task.completionProof,
                    canReview = true,
                    onAccept = { viewModel.acceptCompletion(task._id) },
                    onReject = { reason -> viewModel.rejectCompletion(task._id, reason) }
                )"""

content = content.replace(poster_review_old, poster_review_new)

# POSTER_PROOF_REJECTED update
poster_rejected_old = """            TaskUiState.POSTER_PROOF_REJECTED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEE0000).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ Proof Rejected", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                        Spacer(Modifier.height(4.dp))
                        Text("Waiting for helper to resubmit.", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                    }
                }"""
poster_rejected_new = """            TaskUiState.POSTER_PROOF_REJECTED -> {
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
                }"""

content = content.replace(poster_rejected_old, poster_rejected_new)

# Add ProofGallery import if not present
if "ProofGallery" not in content:
    content = content.replace("import com.strangerhelp.app.data.model.Task", "import com.strangerhelp.app.data.model.Task\nimport com.strangerhelp.app.ui.screens.tasks.ProofGallery")

# Add ProofGallery import for missing one
content = content.replace("import com.strangerhelp.app.data.model.Task\nimport com.strangerhelp.app.ui.screens.tasks.ProofGallery", "import com.strangerhelp.app.data.model.Task")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)
