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


enum class TaskUiState {
    VISITOR_CAN_REQUEST, VISITOR_MUST_LOGIN,
    HELPER_CAN_REQUEST, HELPER_REQUEST_PENDING, HELPER_REQUEST_REJECTED,
    HELPER_CLAIMED_CAN_TRACK, HELPER_TRACKING, HELPER_SUBMIT_PROOF,
    HELPER_PROOF_PENDING, HELPER_PROOF_REJECTED, HELPER_COMPLETED,
    POSTER_WAITING, POSTER_HAS_REQUESTS, POSTER_CLAIMED_WAITING,
    POSTER_REVIEW_PROOF, POSTER_PROOF_REJECTED, POSTER_COMPLETED,
}

fun resolveState(task: Task?, me: User?): TaskUiState {
    if (task == null) return TaskUiState.VISITOR_MUST_LOGIN
    val isOwner = me != null && task.posterId == me.id
    val isClaimer = me != null && (task.claimedBy == me.id || task.claimedUsers?.any { it.userId == me.id } == true)
    val myReq = task.claimRequests?.find { it.requesterId == me?.id }

    return when {
        // ---- POSTER ----
        isOwner && task.status == "open" && task.claimRequests?.any { it.status == "pending" } == true -> TaskUiState.POSTER_HAS_REQUESTS
        isOwner && task.status == "open" -> TaskUiState.POSTER_WAITING
        isOwner && task.status == "claimed" && task.completionStatus == "pending" -> TaskUiState.POSTER_REVIEW_PROOF
        isOwner && task.status == "claimed" && task.completionStatus == "rejected" -> TaskUiState.POSTER_PROOF_REJECTED
        isOwner && task.status == "claimed" -> TaskUiState.POSTER_CLAIMED_WAITING
        isOwner && task.status == "completed" -> TaskUiState.POSTER_COMPLETED

        // ---- HELPER (claimer) ----
        isClaimer && task.completionStatus == "pending" -> TaskUiState.HELPER_PROOF_PENDING
        isClaimer && task.completionStatus == "rejected" -> TaskUiState.HELPER_PROOF_REJECTED
        isClaimer && task.completionStatus == "accepted" -> TaskUiState.HELPER_COMPLETED
        isClaimer && task.trackingActive -> TaskUiState.HELPER_TRACKING
        isClaimer && task.status == "claimed" -> TaskUiState.HELPER_CLAIMED_CAN_TRACK

        // ---- HELPER (not yet claimed) ----
        me != null && task.status == "open" && myReq?.status == "pending" -> TaskUiState.HELPER_REQUEST_PENDING
        me != null && task.status == "open" && myReq?.status == "rejected" -> TaskUiState.HELPER_REQUEST_REJECTED
        me != null && task.status == "open" -> TaskUiState.HELPER_CAN_REQUEST

        // ---- VISITOR ----
        me == null && task.status == "open" -> TaskUiState.VISITOR_MUST_LOGIN
        else -> TaskUiState.POSTER_COMPLETED   // fallback: view-only
    }
}

class TaskDetailViewModel(
    private val taskRepository: TaskRepository = TaskRepository(ApiClient.api),
    private val authRepository: AuthRepository = AuthRepository(ApiClient.api)
) : ViewModel() {

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    private val _isProcessingClaim = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isProcessingClaim: kotlinx.coroutines.flow.StateFlow<Boolean> = _isProcessingClaim.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()



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

    val uiState: kotlinx.coroutines.flow.Flow<TaskUiState> = kotlinx.coroutines.flow.combine(_task, _currentUser) { task, user ->
        resolveState(task, user)
    }

    private val _navigateToChat = MutableSharedFlow<String>()
    val navigateToChat: SharedFlow<String> = _navigateToChat

    init {
        loadCurrentUser()
        startPolling()
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

    
    private val _navigateToGpsCamera = MutableSharedFlow<String>()
    val navigateToGpsCamera: SharedFlow<String> = _navigateToGpsCamera

    fun openGpsCamera() {
        viewModelScope.launch {
            _navigateToGpsCamera.emit(task.value?._id ?: "")
        }
    }

    fun loadTask(taskId: String, isBackgroundSync: Boolean = false) {
        viewModelScope.launch {
            if (_task.value == null) { _isLoading.value = true }
            if (isBackgroundSync) { _isSyncing.value = true }
            _error.value = null

            try {
                val response = taskRepository.getTask(taskId)
                if (response.isSuccessful) {
                    _task.value = response.body()
                    updateClaimState(_task.value)
                    _isOffline.value = false
                } else {
                    if (!isBackgroundSync) {
                        _error.value = parseError(response.errorBody()?.string())
                    } else {
                        _isOffline.value = true
                    }
                }
            } catch (e: Exception) {
                if (!isBackgroundSync) {
                    _error.value = "Failed to load task"
                } else {
                    _isOffline.value = true
                }
            } finally {
                _isLoading.value = false
                if (isBackgroundSync) { _isSyncing.value = false }
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                _task.value?._id?.let { id ->
                    loadTask(id, isBackgroundSync = true)
                }
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
    }

    fun rejectClaim(taskId: String, requesterId: String) {
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

    
    fun editTask(taskId: String, updates: Map<String, Any>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = taskRepository.patchTask(taskId, updates) // Assuming updateTracking actually calls PATCH /api/tasks/{id} which can do edits if we just pass a map. Wait, let me check TaskRepository.
                if (response.isSuccessful) {
                    loadTask(taskId)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
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


    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    fun startTracking(taskId: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("action" to "start_tracking")
                val res = taskRepository.patchTask(taskId, body)
                if (res.isSuccessful) {
                    _isTracking.value = true
                    loadTask(taskId)
                }
            } catch (_: Exception) { }
        }
    }

    fun startTrackingWithService(taskId: String, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val body = mapOf("action" to "start_tracking")
                val res = taskRepository.patchTask(taskId, body)
                if (res.isSuccessful) {
                    com.strangerhelp.app.service.TrackingService.start(context, taskId)
                    _isTracking.value = true
                    loadTask(taskId)
                }
            } catch (_: Exception) { }
        }
    }

    fun stopTracking(taskId: String, context: android.content.Context? = null) {
        viewModelScope.launch {
            try {
                if (context != null) {
                    com.strangerhelp.app.service.TrackingService.stop(context)
                }
                val body = mapOf("action" to "stop_tracking")
                val res = taskRepository.patchTask(taskId, body)
                if (res.isSuccessful) {
                    _isTracking.value = false
                    loadTask(taskId)
                }
            } catch (_: Exception) { }
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

