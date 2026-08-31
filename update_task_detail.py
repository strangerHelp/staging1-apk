import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    text = f.read()

# Replace LaunchedEffect
old_effect = """    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    LaunchedEffect(task?.trackingActive, currentUser?.id) {
        val intent = android.content.Intent(context, com.strangerhelp.app.service.TrackingService::class.java).apply {
            putExtra("taskId", taskId)
        }
        if (task?.trackingActive == true && currentUser?.id == task?.claimedBy) {
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        } else {
            context.stopService(intent)
        }
    }"""

new_effect = """    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
        viewModel.startPosterPolling(taskId)
    }"""

text = text.replace(old_effect, new_effect)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(text)
