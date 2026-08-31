import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# Replace from `fun loadTask` to `private fun startPolling()`
start_idx = content.find("    fun loadTask(taskId: String")
end_idx = content.find("    private fun startPolling()")

if start_idx != -1 and end_idx != -1:
    correct_load_task = """    fun loadTask(taskId: String, isBackgroundSync: Boolean = false) {
        viewModelScope.launch {
            if (_task.value == null) { _isLoading.value = true }
            if (isBackgroundSync) { _isSyncing.value = true }
            _error.value = null

            try {
                val response = taskRepository.getTask(taskId)
                if (response.isSuccessful) {
                    _task.value = response.body()
                    updateClaimState(_task.value)
                } else {
                    if (!isBackgroundSync) {
                        _error.value = parseError(response.errorBody()?.string())
                    }
                }
            } catch (e: Exception) {
                if (!isBackgroundSync) {
                    _error.value = "Failed to load task"
                }
            } finally {
                _isLoading.value = false
                if (isBackgroundSync) { _isSyncing.value = false }
            }
        }
    }

"""
    content = content[:start_idx] + correct_load_task + content[end_idx:]
    with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
        f.write(content)
