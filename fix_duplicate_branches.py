import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    text = f.read()

bad_block = """            TaskUiState.POSTER_WAITING, TaskUiState.POSTER_HAS_REQUESTS -> {
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
                Button(
                    onClick = { 
                        task.claimedBy?.let { claimerId ->
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
            }
            TaskUiState.POSTER_REVIEW_PROOF -> {
                Card(colors = CardDefaults.cardColors(containerColor = CyanDeep.copy(alpha = 0.1f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Proof Submitted", fontWeight = FontWeight.Bold, color = CyanDeep)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.rejectCompletion(task._id, "Needs revision") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFEE0000))) { Text("Reject") }
                            Button(onClick = { viewModel.acceptCompletion(task._id) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981))) { Text("Accept") }
                        }
                    }
                }
            }
            TaskUiState.POSTER_PROOF_REJECTED -> {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEE0000).copy(alpha = 0.1f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("❌ Proof Rejected", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                        Text("Waiting for helper to resubmit proof.", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                    }
                }
            }
            TaskUiState.POSTER_COMPLETED -> {
                Button(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                    Text("⭐ Leave a Review")
                }
            }"""

text = text.replace(bad_block, "")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(text)
