with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# I need to find the startPolling function and remove the extra closing braces after it.
import re

content = re.sub(r'    private fun startPolling\(\) \{\n        pollJob\?\.cancel\(\)\n        pollJob = viewModelScope\.launch \{\n            while \(isActive\) \{\n                delay\(5000\)\n                _task\.value\?._id\?\.let \{ id ->\n                    loadTask\(id, isBackgroundSync = true\)\n                \}\n            \}\n        \}\n    \}\n            \}\n        \}\n    \}\n\nfun requestToClaim\(', '    private fun startPolling() {\n        pollJob?.cancel()\n        pollJob = viewModelScope.launch {\n            while (isActive) {\n                delay(5000)\n                _task.value?._id?.let { id ->\n                    loadTask(id, isBackgroundSync = true)\n                }\n            }\n        }\n    }\n\n    fun requestToClaim(', content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
