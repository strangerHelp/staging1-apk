import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

import_monitor = "import com.strangerhelp.app.utils.BatteryMonitor\n"
if "import com.strangerhelp.app.utils.BatteryMonitor" not in content:
    content = content.replace("import com.strangerhelp.app.data.repository.AuthRepository\n", "import com.strangerhelp.app.data.repository.AuthRepository\n" + import_monitor)

old_polling = """    private fun startPolling() {
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

new_polling = """    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                val delayTime = if (BatteryMonitor.isBatterySaverMode.value) 15000L else 5000L
                delay(delayTime)
                _task.value?._id?.let { id ->
                    loadTask(id, isBackgroundSync = true)
                }
            }
        }
    }"""

content = content.replace(old_polling, new_polling)

with open(file_path, "w") as f:
    f.write(content)
