import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    text = f.read()

edit_delete_buttons = """                Spacer(Modifier.height(8.dp))
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
                }"""

# Add to POSTER_WAITING
poster_waiting = """            TaskUiState.POSTER_WAITING -> {
                Card(colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Waiting for helpers to request your task.", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)
                    }
                }"""

new_poster_waiting = poster_waiting + "\n" + edit_delete_buttons

text = text.replace(poster_waiting, new_poster_waiting)

# Add to POSTER_HAS_REQUESTS
poster_has_requests = """                        onReject = { viewModel.rejectClaim(task._id, request.requesterId) }
                    )
                }"""

new_poster_has_requests = poster_has_requests + "\n" + edit_delete_buttons

text = text.replace(poster_has_requests, new_poster_has_requests)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(text)
