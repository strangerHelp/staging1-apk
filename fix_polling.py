import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# 1. Update init block
init_match = re.search(r'    init \{\n        loadCurrentUser\(\)\n    \}', content)
if init_match:
    content = content.replace(init_match.group(0), '    init {\n        loadCurrentUser()\n        startPolling()\n    }')

# 2. Update startPosterPolling / startPolling
def replacer(m):
    return ""

content = re.sub(r'    fun startPosterPolling\(taskId: String\) \{.*?    \}\n', '', content, flags=re.DOTALL)

# 3. Modify startPolling(taskId: String) to just startPolling()
start_polling = """    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                _task.value?._id?.let { id ->
                    try {
                        val response = taskRepository.getTask(id)
                        if (response.isSuccessful) {
                            _task.value = response.body()
                            updateClaimState(_task.value)
                        }
                    } catch (e: Exception) { }
                }
            }
        }
    }"""
content = re.sub(r'    fun startPolling\(taskId: String\) \{.*?    \}\n', start_polling + '\n', content, flags=re.DOTALL)

# 4. Remove startPolling(taskId) call from loadTask
content = re.sub(r'\n                    startPolling\(taskId\)', '', content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
