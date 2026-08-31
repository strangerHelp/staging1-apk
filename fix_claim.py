import re

# 1. Update TaskDetailViewModel.kt
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

if "isProcessingClaim" not in content:
    content = content.replace(
        'val task: StateFlow<Task?> = _task.asStateFlow()',
        'val task: StateFlow<Task?> = _task.asStateFlow()\n\n    private val _isProcessingClaim = kotlinx.coroutines.flow.MutableStateFlow(false)\n    val isProcessingClaim: kotlinx.coroutines.flow.StateFlow<Boolean> = _isProcessingClaim.asStateFlow()'
    )

    approve_content = """    fun approveClaim(taskId: String, requesterId: String) {
        viewModelScope.launch {
            _isProcessingClaim.value = true
            try {
                val body = mapOf("action" to "approve_claim", "requesterId" to requesterId)
                val response = taskRepository.patchTask(taskId, body)
                if (response.isSuccessful) {
                    loadTask(taskId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (response.code() == 409) {
                        _error.value = "This task has already been claimed by someone else."
                        loadTask(taskId)
                    } else {
                        _error.value = parseError(errorBody)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to approve claim"
            } finally {
                _isProcessingClaim.value = false
            }
        }
    }"""
    
    # regex replace approveClaim
    content = re.sub(r'    fun approveClaim\(.*?\}', approve_content, content, flags=re.DOTALL)
    
    reject_content = """    fun rejectClaim(taskId: String, requesterId: String) {
        viewModelScope.launch {
            _isProcessingClaim.value = true
            try {
                val body = mapOf("action" to "reject_claim", "requesterId" to requesterId)
                val response = taskRepository.patchTask(taskId, body)
                if (response.isSuccessful) {
                    loadTask(taskId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to reject claim"
            } finally {
                _isProcessingClaim.value = false
            }
        }
    }"""
    content = re.sub(r'    fun rejectClaim\(.*?\}', reject_content, content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)

# 2. Update PosterComponents.kt (ClaimRequestCard)
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

if "isProcessing: Boolean = false" not in content:
    content = content.replace(
        'fun ClaimRequestCard(\n    request: ClaimRequest,\n    taskBudget: Int,\n    onApprove: () -> Unit,\n    onReject: () -> Unit\n)',
        'fun ClaimRequestCard(\n    request: ClaimRequest,\n    taskBudget: Int,\n    onApprove: () -> Unit,\n    onReject: () -> Unit,\n    isProcessing: Boolean = false\n)'
    )
    
    content = content.replace(
        '                Button(\n                    onClick = onApprove,\n                    colors = ButtonDefaults.buttonColors(\n                        containerColor = androidx.compose.ui.graphics.Color(0xFF10B981)\n                    ),\n                    modifier = Modifier.weight(1f)\n                ) {\n                    Text("✅ Approve", color = OnPrimary)\n                }',
        '                Button(\n                    onClick = onApprove,\n                    enabled = !isProcessing,\n                    colors = ButtonDefaults.buttonColors(\n                        containerColor = androidx.compose.ui.graphics.Color(0xFF10B981)\n                    ),\n                    modifier = Modifier.weight(1f)\n                ) {\n                    if (isProcessing) {\n                        CircularProgressIndicator(\n                            modifier = Modifier.size(16.dp),\n                            color = OnPrimary,\n                            strokeWidth = 2.dp\n                        )\n                    } else {\n                        Text("✅ Approve", color = OnPrimary)\n                    }\n                }'
    )
    
    content = content.replace(
        '                OutlinedButton(\n                    onClick = onReject,\n                    colors = ButtonDefaults.outlinedButtonColors(\n                        contentColor = Error\n                    ),\n                    modifier = Modifier.weight(1f)\n                ) {\n                    Text("❌ Reject")\n                }',
        '                OutlinedButton(\n                    onClick = onReject,\n                    enabled = !isProcessing,\n                    colors = ButtonDefaults.outlinedButtonColors(\n                        contentColor = Error\n                    ),\n                    modifier = Modifier.weight(1f)\n                ) {\n                    Text("❌ Reject")\n                }'
    )
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)

# 3. Update TaskActionSection.kt (Pass isProcessing to ClaimRequestCard)
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

if "val isProcessing by viewModel.isProcessingClaim.collectAsState()" not in content:
    content = content.replace(
        'fun TaskActionSection(\n    uiState: TaskUiState,\n    task: Task,\n    viewModel: TaskDetailViewModel,\n    navController: NavController,\n    onDelete: () -> Unit,\n    onEdit: () -> Unit,\n    onReview: () -> Unit\n) {',
        'fun TaskActionSection(\n    uiState: TaskUiState,\n    task: Task,\n    viewModel: TaskDetailViewModel,\n    navController: NavController,\n    onDelete: () -> Unit,\n    onEdit: () -> Unit,\n    onReview: () -> Unit\n) {\n    val isProcessing by viewModel.isProcessingClaim.collectAsState()'
    )
    
    content = content.replace(
        '                    ClaimRequestCard(\n                        request = request,\n                        taskBudget = task.budget,\n                        onApprove = { viewModel.approveClaim(task._id, request.requesterId) },\n                        onReject = { viewModel.rejectClaim(task._id, request.requesterId) }\n                    )',
        '                    ClaimRequestCard(\n                        request = request,\n                        taskBudget = task.budget,\n                        onApprove = { viewModel.approveClaim(task._id, request.requesterId) },\n                        onReject = { viewModel.rejectClaim(task._id, request.requesterId) },\n                        isProcessing = isProcessing\n                    )'
    )

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)
