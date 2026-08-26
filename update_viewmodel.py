import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt', 'r') as f:
    content = f.read()

claim_status_enum = """
    enum class ClaimStatus {
        NONE,           // No claim request
        REQUESTED,      // Request sent, waiting for approval
        APPROVED,       // Approved (task claimed)
        REJECTED        // Rejected
    }

    private val _claimStatus = MutableStateFlow<ClaimStatus>(ClaimStatus.NONE)
    val claimStatus: StateFlow<ClaimStatus> = _claimStatus.asStateFlow()
"""

# Insert claim status enum before init
content = content.replace("    init {", claim_status_enum + "\n    init {")

# Replace claimTask
old_claim_task = """
    fun claimTask(taskId: String, offeredBudget: Int?, message: String?) {
        viewModelScope.launch {
            _isClaiming.value = true
            _error.value = null
            try {
                val body = mapOf(
                    "action" to "claim",
                    "offered_budget" to offeredBudget,
                    "message" to message
                ).filterValues { it != null }
                val response = taskRepository.patchTask(taskId, body)
                if (response.isSuccessful) {
                    loadTask(taskId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to claim task"
            } finally {
                _isClaiming.value = false
            }
        }
    }
"""

new_claim_task = """
    fun claimTask(
        taskId: String,
        offeredBudget: Int?,
        message: String?,
        onSuccess: (conversationId: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isClaiming.value = true
            _error.value = null
            try {
                val body = mapOf(
                    "action" to "claim",
                    "offered_budget" to offeredBudget,
                    "message" to message
                ).filterValues { it != null }
                
                val response = taskRepository.patchTask(taskId, body)
                
                if (response.isSuccessful) {
                    val result = response.body()
                    val status = result?.get("status")?.toString()?.replace("\"", "")
                    val conversationId = result?.get("conversationId")?.toString()?.replace("\"", "")
                    
                    if (status == "requested" || status == "pending") {
                        _claimStatus.value = ClaimStatus.REQUESTED
                        if (!conversationId.isNullOrEmpty()) {
                            onSuccess(conversationId)
                        }
                    }
                    loadTask(taskId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to claim task"
            } finally {
                _isClaiming.value = false
            }
        }
    }

    fun getClaimStatusForUser(task: Task?, userId: String?): ClaimStatus {
        if (task == null || userId == null) return ClaimStatus.NONE
        val myRequest = task.claimRequests?.find { it.requesterId == userId }
        return when (myRequest?.status) {
            "pending" -> ClaimStatus.REQUESTED
            "approved" -> ClaimStatus.APPROVED
            "rejected" -> ClaimStatus.REJECTED
            else -> ClaimStatus.NONE
        }
    }

    fun isCurrentUserClaimer(task: Task?, userId: String?): Boolean {
        if (task == null || userId == null) return false
        return task.claimedBy == userId
    }
"""

content = content.replace(old_claim_task.strip(), new_claim_task.strip())

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt', 'w') as f:
    f.write(content)
