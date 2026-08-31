import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# I will find startPolling completely and replace it
start_idx = content.find("    private fun startPolling()")
end_idx = content.find("    fun requestToClaim")

if start_idx != -1 and end_idx != -1:
    correct_start_polling = """    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                _task.value?._id?.let { id ->
                    loadTask(id)
                }
            }
        }
    }

"""
    content = content[:start_idx] + correct_start_polling + content[end_idx:]
    with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
        f.write(content)

