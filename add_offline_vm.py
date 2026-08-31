import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# Add _isOffline
offline_state = """
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
"""
if "val isOffline" not in content:
    content = content.replace(
        "val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()",
        "val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()\n" + offline_state
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
    }"""
content = re.sub(r'    fun loadTask\(taskId: String.*?    \}\n', load_task_repl + '\n', content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
