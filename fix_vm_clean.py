import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# We want to replace from `fun loadTask(taskId: String` to `fun requestToClaim(`

start_idx = content.find("    fun loadTask(taskId: String")
end_idx = content.find("fun requestToClaim(")

if end_idx == -1:
    end_idx = content.find("    fun requestToClaim(")

correct_code = """    fun loadTask(taskId: String, isBackgroundSync: Boolean = false) {
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

    """

content = content[:start_idx] + correct_code + content[end_idx:]

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
