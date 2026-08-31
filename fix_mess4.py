with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    text = f.read()

import re

# We will just replace everything from TaskUiState.HELPER_PROOF_PENDING to TaskUiState.POSTER_WAITING
replacement = """            TaskUiState.HELPER_PROOF_PENDING -> {
                Card(colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Proof submitted — waiting for poster to review", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            }
            TaskUiState.HELPER_PROOF_REJECTED -> {
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
                HelperProofGallery(task.completionProof)
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
                Button(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                    Text("⭐ Leave a Review")
                }
                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            }
            TaskUiState.POSTER_WAITING -> {"""

text = re.sub(r'TaskUiState\.HELPER_PROOF_PENDING -> \{.*?TaskUiState\.POSTER_WAITING -> \{', replacement, text, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(text)

