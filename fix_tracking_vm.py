import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    text = f.read()

# First replace startTracking and stopTracking and updateLocation with the new service-based logic
old_tracking_logic = """    fun startTracking(taskId: String) {
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

    fun updateLocation(taskId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val body = mapOf("action" to "update_location", "lat" to lat, "lng" to lng)
                taskRepository.patchTask(taskId, body)
            } catch (_: Exception) { }
        }
    }"""

new_tracking_logic = """
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
"""

text = text.replace(old_tracking_logic, new_tracking_logic)

# Replace startPosterPolling
# Add startPosterPolling inside the class body, near loadTask maybe? No, let's just replace `private var pollJob: Job? = null` with the polling logic.
old_poll = "private var pollJob: Job? = null"
new_poll = """private var pollJob: Job? = null

    fun startPosterPolling(taskId: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                val t = _task.value
                if (t?.trackingActive == true) {
                    delay(5000)
                    loadTask(taskId)
                } else {
                    delay(30000)
                }
            }
        }
    }"""

text = text.replace(old_poll, new_poll)

# And in loadTask, update _isTracking based on task
update_logic = """
                    _task.value = task
                    _isTracking.value = task.trackingActive == true
"""
text = text.replace("_task.value = task\n", update_logic)

# And for submitProof, autoStopTracking.
submit_proof_old = """    fun submitProof(taskId: String, proofBytes: ByteArray, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = taskRepository.submitProofBytes(taskId, proofBytes)
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                    onResult(false)
                }
            } catch (e: Exception) {
                _error.value = "Failed to submit proof"
                onResult(false)
            }
        }
    }"""
submit_proof_new = """    fun submitProof(taskId: String, proofBytes: ByteArray, context: android.content.Context? = null, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = taskRepository.submitProofBytes(taskId, proofBytes)
                if (response.isSuccessful) {
                    if (context != null) {
                        com.strangerhelp.app.service.TrackingService.stop(context)
                        _isTracking.value = false
                    }
                    onResult(true)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                    onResult(false)
                }
            } catch (e: Exception) {
                _error.value = "Failed to submit proof"
                onResult(false)
            }
        }
    }"""
text = text.replace(submit_proof_old, submit_proof_new)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(text)
