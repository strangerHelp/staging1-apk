import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

target_top_app_bar = """                actions = {
                    IconButton(onClick = { task?.let { shareTask(navController.context, it) } }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    IconButton(onClick = { /* More Options */ }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                }"""

replacement_top_app_bar = """                actions = {
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
                                    text = { Text("Delete Task", color = ErrorColor) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmation = true
                                    }
                                )
                            }
                        }
                    }
                }"""

target_state_vars = """    val error by viewModel.error.collectAsStateWithLifecycle()
    val isSubmittingProof by viewModel.isSubmittingProof.collectAsStateWithLifecycle()

    var showDeleteConfirmation by remember { mutableStateOf(false) }"""

replacement_state_vars = """    val error by viewModel.error.collectAsStateWithLifecycle()
    val isSubmittingProof by viewModel.isSubmittingProof.collectAsStateWithLifecycle()

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }"""

target_lazy_column_top = """                    // 1. Task Header
                    item {
                        TaskHeader(t)
                    }"""

replacement_lazy_column_top = """                    // 1. Task Header
                    item {
                        TaskHeader(t)
                        if (t.maxClaimers > 1) {
                            Spacer(Modifier.height(16.dp))
                            GroupProgressSection(t.claimedUsers, t.maxClaimers)
                        }
                        if (t.visibility == "private" && currentUser?.id == t.posterId) {
                            Spacer(Modifier.height(16.dp))
                            PrivateTaskInviteLink(t._id, t.inviteCode ?: "")
                        }
                    }"""

target_claim_requests = """                    if (currentUser?.id == t.posterId && t.status == "open") {
                        item {
                            ClaimRequestsSection(t, viewModel)
                        }
                    }"""

replacement_claim_requests = """                    // ClaimRequestsSection removed since it is now in TaskActionSection"""

target_dialogs = """    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(taskId) { success ->
                            if (success) navController.popBackStack()
                        }
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }"""

replacement_dialogs = """    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(taskId) { success ->
                            if (success) navController.popBackStack()
                        }
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditDialog && task != null) {
        EditTaskDialog(
            task = task!!,
            onDismiss = { showEditDialog = false },
            onSave = { updates ->
                showEditDialog = false
                viewModel.editTask(taskId, updates) { success ->
                    if (success) {
                        Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }"""

content = content.replace(target_top_app_bar, replacement_top_app_bar)
content = content.replace(target_state_vars, replacement_state_vars)
content = content.replace(target_lazy_column_top, replacement_lazy_column_top)
content = content.replace(target_claim_requests, replacement_claim_requests)
content = content.replace(target_dialogs, replacement_dialogs)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
