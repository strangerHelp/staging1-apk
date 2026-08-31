import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

target = """    fun deleteTask(taskId: String, onResult: (Boolean) -> Unit) {"""

replacement = """    fun editTask(taskId: String, updates: Map<String, Any>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = taskRepository.updateTracking(taskId, updates) // Assuming updateTracking actually calls PATCH /api/tasks/{id} which can do edits if we just pass a map. Wait, let me check TaskRepository.
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

    fun deleteTask(taskId: String, onResult: (Boolean) -> Unit) {"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
