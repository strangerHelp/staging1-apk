import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# Add _isSyncing
syncing_state = """
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
"""
if "val isSyncing" not in content:
    content = content.replace(
        "val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()",
        "val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()\n" + syncing_state
    )

# Modify loadTask
load_task_repl = """    fun loadTask(taskId: String, isBackgroundSync: Boolean = false) {
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
    }"""
content = re.sub(r'    fun loadTask\(taskId: String\) \{.*?    \}\n', load_task_repl + '\n', content, flags=re.DOTALL)

# Modify startPolling
start_polling_repl = """    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                _task.value?._id?.let { id ->
                    loadTask(id, isBackgroundSync = true)
                }
            }
        }
    }"""
content = re.sub(r'    private fun startPolling\(\) \{.*?    \}\n', start_polling_repl + '\n', content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
