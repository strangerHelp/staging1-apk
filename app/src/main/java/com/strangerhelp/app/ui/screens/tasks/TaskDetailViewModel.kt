package com.strangerhelp.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.StrangerHelpApp
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Conversation
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.model.ClaimResponse
import com.strangerhelp.app.data.model.ClaimTaskRequest
import com.strangerhelp.app.data.repository.AuthRepository
import com.strangerhelp.app.utils.AppLogger
import com.strangerhelp.app.utils.BatteryMonitor
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
    CLAIMED_VIEWER,
}

fun resolveState(task: Task?, me: User?): TaskUiState {
    if (task == null) return TaskUiState.VISITOR_MUST_LOGIN
    val isOwner = me != null && task.posterId == me.id
    val isClaimer = me != null && (
        task.claimedBy == me.id ||
        task.claimedUsers?.any { it.userId == me.id } == true ||
        task.claimRequests?.any { it.requesterId == me.id && it.status == "approved" } == true
    )
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

        // ---- CLAIMED TASK (Viewer/Visitor) ----
        task.status == "claimed" -> TaskUiState.CLAIMED_VIEWER

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
        val cleanTaskId = taskId.trim().removeSurrounding("\"").substringBefore('?').substringBefore('#').trimEnd('/')
        if (cleanTaskId.isBlank()) {
            if (!isBackgroundSync) _error.value = "Invalid task ID"
            return
        }

        viewModelScope.launch {
            if (_task.value == null) { _isLoading.value = true }
            if (isBackgroundSync) { _isSyncing.value = true }
            _error.value = null

            // 1. Immediately display from Room local database cache if available
            if (_task.value == null) {
                try {
                    val localTask = taskRepository.getTaskFromCache(cleanTaskId)
                    if (localTask != null) {
                        _task.value = localTask
                        updateClaimState(localTask)
                        _isLoading.value = false
                    }
                } catch (_: Exception) {}
            }

            // 2. Fetch fresh task from API
            try {
                val response = taskRepository.getTask(cleanTaskId)
                if (response.isSuccessful) {
                    val taskBody = response.body()
                    if (taskBody != null) {
                        _task.value = taskBody
                        updateClaimState(taskBody)
                        _isOffline.value = false
                    } else {
                        val cached = taskRepository.getTaskFromCache(cleanTaskId)
                        if (cached != null) {
                            _task.value = cached
                            updateClaimState(cached)
                            _isOffline.value = true
                        } else if (!isBackgroundSync) {
                            _error.value = "Task not found or has been removed"
                        }
                    }
                } else {
                    val cached = taskRepository.getTaskFromCache(cleanTaskId)
                    if (cached != null) {
                        _task.value = cached
                        updateClaimState(cached)
                        _isOffline.value = true
                    } else if (!isBackgroundSync) {
                        val errorDetail = parseError(response.errorBody()?.string())
                        _error.value = if (response.code() == 404) {
                            "Task not found or has been removed"
                        } else {
                            errorDetail.takeIf { it != "Something went wrong" } ?: "Failed to load task (${response.code()})"
                        }
                    } else {
                        _isOffline.value = true
                    }
                }
            } catch (e: Exception) {
                AppLogger.w("TaskDetailVM", "Exception loading task $cleanTaskId: ${e.message}")
                val cached = taskRepository.getTaskFromCache(cleanTaskId)
                if (cached != null) {
                    _task.value = cached
                    updateClaimState(cached)
                    _isOffline.value = true
                } else if (_task.value == null) {
                    _isOffline.value = true
                    val fallback = Task(
                        _id = cleanTaskId,
                        title = "Offline Task",
                        description = "Viewing in offline mode. Connect to internet to see full details.",
                        status = "open",
                        deadline = "Today",
                        budget = 0
                    )
                    _task.value = fallback
                    updateClaimState(fallback)
                    if (!isBackgroundSync) {
                        _error.value = "Offline mode: viewing offline task"
                    }
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
                val delayTime = if (BatteryMonitor.isBatterySaverMode.value) 15000L else 5000L
                delay(delayTime)
                if (_isOffline.value || !com.strangerhelp.app.utils.NetworkMonitor.isCurrentlyOnline()) {
                    delay(15000L)
                    continue
                }
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
        val currentTask = _task.value
        val currentUserId = _currentUser.value?.id?.takeIf { it.isNotBlank() } ?: "current_user"
        val currentUserName = _currentUser.value?.name?.takeIf { it.isNotBlank() } ?: "You"
        val effectivePosterId = posterId.ifBlank { currentTask?.posterId.orEmpty().ifBlank { "poster_${taskId}" } }
        val posterName = currentTask?.posterName?.takeIf { it.isNotBlank() } ?: "Poster"

        viewModelScope.launch {
            val db = runCatching { StrangerHelpApp.instance.database }.getOrNull()
            var conversationId: String? = null

            // 1. Check local Room database first for existing conversation for this task
            try {
                val existing = db?.conversationDao()?.getConversationByTaskId(taskId)
                    ?: db?.conversationDao()?.getConversationById("task_conv_$taskId")
                if (existing != null && existing._id.isNotBlank()) {
                    conversationId = existing._id
                    AppLogger.d("TaskDetailVM", "Found existing local conversation: $conversationId for task $taskId")
                }
            } catch (e: Exception) {
                AppLogger.w("TaskDetailVM", "Error checking existing local conversation", e)
            }

            // 2. Pre-create local/fallback conversation so ChatDetailScreen opens instantly
            val finalConvId = conversationId ?: "task_conv_${taskId}"
            try {
                val fallbackConv = Conversation(
                    _id = finalConvId,
                    taskId = taskId,
                    participants = listOf(currentUserId, effectivePosterId),
                    participantNames = listOf(currentUserName, posterName),
                    lastMessage = "Discussion for task: ${currentTask?.title.orEmpty()}",
                    lastMessageAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                )
                db?.conversationDao()?.insertConversations(listOf(fallbackConv))
            } catch (e: Exception) {
                AppLogger.w("TaskDetailVM", "Failed to cache fallback conversation", e)
            }

            // 3. Immediately trigger navigation callback so user is redirected to message page with 0 lag
            AppLogger.i("TaskDetailVM", "Redirecting immediately to message page with conversationId: $finalConvId")
            onResult(finalConvId)

            // 4. In background, sync with server if not already a remote conversation
            if (conversationId == null && effectivePosterId.isNotBlank() && !effectivePosterId.startsWith("poster_")) {
                try {
                    val body = mapOf("recipientId" to effectivePosterId, "taskId" to taskId)
                    val response = taskRepository.createConversation(body)
                    if (response.isSuccessful) {
                        val conv = response.body()
                        if (conv != null && conv._id.isNotBlank()) {
                            AppLogger.d("TaskDetailVM", "Synced remote conversation: ${conv._id}")
                            db?.conversationDao()?.insertConversations(listOf(conv))
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.w("TaskDetailVM", "Background conversation sync failed: ${e.message}")
                }
            }
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
                val payload = updates.toMutableMap()
                payload["action"] = "edit"
                val response = taskRepository.patchTask(taskId, payload)
                if (response.isSuccessful) {
                    loadTask(taskId)
                    onResult(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = parseError(errorBody)
                    onResult(false)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to edit task"
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


    



    
    private val _isTracking = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isTracking: kotlinx.coroutines.flow.StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _trackingError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val trackingError: kotlinx.coroutines.flow.StateFlow<String?> = _trackingError.asStateFlow()

    fun startTracking(taskId: String, context: android.content.Context) {
        viewModelScope.launch {
            _trackingError.value = null

            if (!hasLocationPermission(context) ) {
                _trackingError.value = "Location permission is required to share your position."
                return@launch
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                !hasNotificationPermission(context)) {
                _trackingError.value =
                    "Enable notifications so the poster can see your position."
                return@launch
            }

            // 1. Tell the server we are starting
            val response = runCatching {
                taskRepository.patchTask(taskId, mapOf("action" to "start_tracking"))
            }.getOrNull()

            if (response?.isSuccessful != true) {
                _trackingError.value = "Could not start tracking. Please retry."
                return@launch
            }

            // 2. Start the foreground service
            com.strangerhelp.app.service.TrackingService.start(context, taskId)
            _isTracking.value = true
            loadTask(taskId)
        }
    }

    fun stopTracking(taskId: String, context: android.content.Context) {
        viewModelScope.launch {
            com.strangerhelp.app.service.TrackingService.stop(context)
            runCatching {
                taskRepository.patchTask(taskId, mapOf("action" to "stop_tracking"))
            }
            _isTracking.value = false
            loadTask(taskId)
        }
    }

    fun autoStopTracking(taskId: String, context: android.content.Context) {
        if (_isTracking.value) {
            stopTracking(taskId, context)
        }
    }

    private fun hasLocationPermission(context: android.content.Context): Boolean {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun hasNotificationPermission(context: android.content.Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
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

