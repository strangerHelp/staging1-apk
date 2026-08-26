import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt', 'r') as f:
    content = f.read()

delete_task_code = """
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
"""

content = content.replace("fun startTracking", delete_task_code + "\n    fun startTracking")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt', 'w') as f:
    f.write(content)
