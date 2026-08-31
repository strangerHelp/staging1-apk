import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

poster_completed_old = """            TaskUiState.POSTER_COMPLETED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Task completed!", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                PosterReviewSection(
                    task = task,
                    onReviewSubmit = { rating, comment ->
                        viewModel.submitReview(task._id, task.claimedBy ?: "", rating, comment)
                    }
                )"""

poster_completed_new = """            TaskUiState.POSTER_COMPLETED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Task completed!", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                ProofGallery(proof = task.completionProof, canReview = false)
                Spacer(Modifier.height(16.dp))
                PosterReviewSection(
                    task = task,
                    onReviewSubmit = { rating, comment ->
                        viewModel.submitReview(task._id, task.claimedBy ?: "", rating, comment)
                    }
                )"""

content = content.replace(poster_completed_old, poster_completed_new)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)
