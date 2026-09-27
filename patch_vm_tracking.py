import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# First, add the appContext property to the constructor if it's not there
if "private val appContext: android.content.Context" not in content and "@dagger.hilt.android.qualifiers.ApplicationContext" not in content:
    content = content.replace("private val taskRepository: TaskRepository", "private val taskRepository: TaskRepository,\n    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context")

# Remove the old methods
methods_to_remove = r"private val _isTracking.*?fun stopTracking.*?catch \(_: Exception\) \{ \}\s*\}\s*\}"
content = re.sub(methods_to_remove, "", content, flags=re.DOTALL)

# Let's verify we got it all out
content = re.sub(r"fun startTracking.*?catch \(_: Exception\) \{ \}\s*\}\s*\}", "", content, flags=re.DOTALL)
content = re.sub(r"fun startTrackingWithService.*?catch \(_: Exception\) \{ \}\s*\}\s*\}", "", content, flags=re.DOTALL)

new_methods = """
    private val _isTracking = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isTracking: kotlinx.coroutines.flow.StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _trackingError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val trackingError: kotlinx.coroutines.flow.StateFlow<String?> = _trackingError.asStateFlow()

    fun startTracking(taskId: String) {
        viewModelScope.launch {
            _trackingError.value = null

            if (!hasLocationPermission() ) {
                _trackingError.value = "Location permission is required to share your position."
                return@launch
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                !hasNotificationPermission()) {
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
            com.strangerhelp.app.service.TrackingService.start(appContext, taskId)
            _isTracking.value = true
        }
    }

    fun stopTracking(taskId: String) {
        viewModelScope.launch {
            com.strangerhelp.app.service.TrackingService.stop(appContext)
            runCatching {
                taskRepository.patchTask(taskId, mapOf("action" to "stop_tracking"))
            }
            _isTracking.value = false
            loadTask(taskId)
        }
    }

    fun autoStopTracking(taskId: String) {
        if (_isTracking.value) {
            stopTracking(taskId)
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(
            appContext, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
            appContext, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun hasNotificationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                appContext, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }
"""

content = content.replace("private fun parseError", new_methods + "\n    private fun parseError")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)

