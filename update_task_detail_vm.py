import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    text = f.read()

inject = """
    private val _navigateToGpsCamera = MutableSharedFlow<String>()
    val navigateToGpsCamera: SharedFlow<String> = _navigateToGpsCamera

    fun openGpsCamera() {
        viewModelScope.launch {
            _navigateToGpsCamera.emit(task.value?._id ?: task.value?.id ?: "")
        }
    }
"""

text = text.replace("fun loadTask(", inject + "\n    fun loadTask(")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(text)
