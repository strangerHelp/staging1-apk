import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

old_code = """    fun editTask(taskId: String, updates: Map<String, Any>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = taskRepository.patchTask(taskId, updates) // Assuming updateTracking actually calls PATCH /api/tasks/{id} which can do edits if we just pass a map. Wait, let me check TaskRepository."""

new_code = """    fun editTask(taskId: String, updates: Map<String, Any>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val payload = updates.toMutableMap()
                payload["action"] = "edit"
                val response = taskRepository.patchTask(taskId, payload)"""

content = content.replace(old_code, new_code)

with open(file_path, "w") as f:
    f.write(content)
