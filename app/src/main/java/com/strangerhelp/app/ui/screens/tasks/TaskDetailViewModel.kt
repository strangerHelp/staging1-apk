package com.strangerhelp.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.model.ClaimResponse
import com.strangerhelp.app.data.model.ClaimTaskRequest
import com.strangerhelp.app.data.repository.AuthRepository
import com.strangerhelp.app.data.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class TaskDetailViewModel(
    private val taskRepository: TaskRepository = TaskRepository(ApiClient.api),
    private val authRepository: AuthRepository = AuthRepository(ApiClient.api)
) : ViewModel() {

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isClaiming = MutableStateFlow(false)
    val isClaiming: StateFlow<Boolean> = _isClaiming.asStateFlow()

    private val _isSubmittingProof = MutableStateFlow(false)
    val isSubmittingProof: StateFlow<Boolean> = _isSubmittingProof.asStateFlow()

    private var pollJob: Job? = null


    enum class ClaimButtonState {
        CAN_CLAIM,          // "Request to Claim" — Primary, clickable
        SENDING,            // "Sending..." — Disabled, loading spinner
        REQUESTED,          // "⏳ Request Sent — Waiting for Approval" — Disabled
        CLAIMED,            // "✓ Claimed" — Disabled, success color
        REJECTED,           // "Request Again" — Primary, clickable
        JOINED              // "✓ Joined (3/5)" — For group tasks, disabled
    }

    private val _claimState = MutableStateFlow(ClaimButtonState.CAN_CLAIM)
    val claimState: StateFlow<ClaimButtonState> = _claimState.asStateFlow()

    private val _navigateToChat = MutableSharedFlow<String>()
    val navigateToChat: SharedFlow<String> = _navigateToChat

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val response = authRepository.getCurrentUser()
                if (response.isSuccessful) {
                    _currentUser.value = response.body()?.user
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = taskRepository.getTask(taskId)
                if (response.isSuccessful) {
                    _task.value = response.body()
                        updateClaimState(_task.value)
                    updateClaimState(_task.value)
                    startPolling(taskId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to load task"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startPolling(taskId: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                try {
                    val response = taskRepository.getTask(taskId)
                    if (response.isSuccessful) {
                        _task.value = response.body()
                        updateClaimState(_task.value)
                    }
                } catch (_: Exception) { }
            }
        }
    }


fun requestToClaim(
        taskId: String,
        offeredBudget: Int?,
        message: String?,
        onSuccess: (conversationId: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isClaiming.value = true
            _claimState.value = ClaimButtonState.SENDING
            _error.value = null

            try {
                val response = taskRepository.claimTask(taskId, offeredBudget, message)
                
                if (response.isSuccessful) {
                    val data = response.body()
                    val status = data?.status
                    val conversationId = data?.conversationId ?: data?.conversation_id
                    
                    if (status == "requested" || status == "pending" || status == "claimed") {
                        _claimState.value = ClaimButtonState.REQUESTED
                        if (!conversationId.isNullOrEmpty()) {
                            onSuccess(conversationId)
                        } else {
                            _error.value = "No conversation created. Please try again."
                            _claimState.value = ClaimButtonState.CAN_CLAIM
                        }
                    }
                    loadTask(taskId)
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    _error.value = errorMsg
                    _claimState.value = ClaimButtonState.CAN_CLAIM
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send request"
                _claimState.value = ClaimButtonState.CAN_CLAIM
            } finally {
                _isClaiming.value = false
            }
        }
    }

    fun messagePoster(taskId: String, posterId: String, onResult: (conversationId: String) -> Unit) {
        viewModelScope.launch {
            try {
                val body = mapOf("recipientId" to posterId, "taskId" to taskId)
                val response = taskRepository.createConversation(body)
                if (response.isSuccessful) {
                    val conv = response.body()
                    if (conv != null) {
                        onResult(conv._id)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun updateClaimState(task: Task?) {
        val userId = _currentUser.value?.id ?: return
        if (task == null) return

        val myRequest = task.claimRequests?.find { it.requesterId == userId }

        _claimState.value = when {
            // User is the claimer (claimedBy == userId)
            task.claimedBy == userId -> ClaimButtonState.CLAIMED

            // User has a pending request
            myRequest?.status == "pending" -> ClaimButtonState.REQUESTED

            // User's request was approved (should be caught by claimedBy above)
            myRequest?.status == "approved" -> ClaimButtonState.CLAIMED

            // User's request was rejected
            myRequest?.status == "rejected" -> ClaimButtonState.REJECTED

            // User is in claimedUsers (group task)
            task.claimedUsers?.any { it.userId == userId } == true -> ClaimButtonState.JOINED

            // No request yet
            else -> ClaimButtonState.CAN_CLAIM
        }
    }

    fun isCurrentUserClaimer(task: Task?, userId: String?): Boolean {
        if (task == null || userId == null) return false
        return task.claimedBy == userId
    }

    fun approveClaim(taskId: String, requesterId: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("action" to "approve_claim", "requesterId" to requesterId)
                val response = taskRepository.patchTask(taskId, body)
                if (response.isSuccessful) {
                    loadTask(taskId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to approve claim"
            }
        }
    }

    fun rejectClaim(taskId: String, requesterId: String) {
        viewModelScope.launch {
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
            }
        }
    }

    fun submitProof(taskId: String, proofFiles: List<File>) {
        viewModelScope.launch {
            _isSubmittingProof.value = true
            _error.value = null

            try {
                val response = taskRepository.submitProof(taskId, proofFiles)
                if (response.isSuccessful) {
                    loadTask(taskId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to submit proof"
            } finally {
                _isSubmittingProof.value = false
            }
        }
    }

    fun acceptCompletion(taskId: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("action" to "accept_completion")
                val response = taskRepository.patchTask(taskId, body)
                if (response.isSuccessful) {
                    loadTask(taskId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to accept completion"
            }
        }
    }

    fun rejectCompletion(taskId: String, reason: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("action" to "reject_completion", "reason" to reason)
                val response = taskRepository.patchTask(taskId, body)
                if (response.isSuccessful) {
                    loadTask(taskId)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to reject completion"
            }
        }
    }

    
    fun deleteTask(taskId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = taskRepository.deleteTask(taskId)
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                    onResult(false)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete task"
                onResult(false)
            }
        }
    }

    fun startTracking(taskId: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("action" to "start_tracking")
                taskRepository.patchTask(taskId, body)
            } catch (_: Exception) { }
        }
    }

    fun stopTracking(taskId: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("action" to "stop_tracking")
                taskRepository.patchTask(taskId, body)
            } catch (_: Exception) { }
        }
    }

        fun submitReview(taskId: String, revieweeId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("taskId" to taskId, "revieweeId" to revieweeId, "rating" to rating, "comment" to comment)
                val response = ApiClient.api.postReview(body)
                if (!response.isSuccessful) {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to submit review"
            }
        }
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Something went wrong"
        return try {
            val json = Gson().fromJson(errorBody, JsonObject::class.java)
            json.get("error")?.asString ?: "Something went wrong"
        } catch (_: Exception) {
            "Something went wrong"
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}

