import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

# Add ReviewDialog and RejectionDialog state variables
state_vars = """    var showClaimDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }"""
content = content.replace("    var showClaimDialog by remember { mutableStateOf(false) }", state_vars)

# Replace the button clicks that just say { /* TODO Review */ } or rejectCompletion
content = content.replace("Button(onClick = { /* TODO Review */ }", "Button(onClick = { showReviewDialog = true }")
content = content.replace("OutlinedButton(onClick = { viewModel.rejectCompletion(task._id, \"Incomplete work\") }", "OutlinedButton(onClick = { showRejectDialog = true }")

# Add dialogs
dialogs = """
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
            
            if (showReviewDialog) {
                ReviewDialog(
                    onDismiss = { showReviewDialog = false },
                    onConfirm = { rating, comment ->
                        val revieweeId = if (isPoster) task!!.claimedBy ?: "" else task!!.posterId
                        viewModel.submitReview(task!!._id, revieweeId, rating, comment)
                        showReviewDialog = false
                    }
                )
            }

            if (showRejectDialog) {
                RejectionDialog(
                    onDismiss = { showRejectDialog = false },
                    onConfirm = { reason ->
                        viewModel.rejectCompletion(task!!._id, reason)
                        showRejectDialog = false
                    }
                )
            }
"""
content = re.sub(r'            if \(showClaimDialog\).*?\}', dialogs, content, flags=re.DOTALL)

# Add dialog Composables
dialog_composables = """
@Composable
fun ReviewDialog(onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave a Review") },
        text = {
            Column {
                Text("Rating: $rating / 5", fontWeight = FontWeight.Bold)
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(rating, comment) }) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RejectionDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject Proof") },
        text = {
            Column {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Rejection") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Text("Reject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

class TaskDetailViewModelFactory
"""

content = content.replace("class TaskDetailViewModelFactory", dialog_composables)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(content)

