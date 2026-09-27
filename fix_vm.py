import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# Fix the constructor
content = re.sub(
    r"private val taskRepository: TaskRepository,\n\s*@dagger\.hilt\.android\.qualifiers\.ApplicationContext private val appContext: android\.content\.Context = TaskRepository\(ApiClient\.api\),",
    "private val taskRepository: TaskRepository = TaskRepository(ApiClient.api),",
    content
)

# Update startTracking and stopTracking to accept context
content = content.replace("fun startTracking(taskId: String) {", "fun startTracking(taskId: String, context: android.content.Context) {")
content = content.replace("fun stopTracking(taskId: String) {", "fun stopTracking(taskId: String, context: android.content.Context) {")
content = content.replace("fun autoStopTracking(taskId: String, context: android.content.Context) {", "fun autoStopTracking(taskId: String, context: android.content.Context) {") # just in case

# Fix the autoStopTracking call - wait, how will it get context?
# Actually autoStopTracking isn't called anywhere right now. We can add context to it:
content = content.replace("fun autoStopTracking(taskId: String) {", "fun autoStopTracking(taskId: String, context: android.content.Context) {")
content = content.replace("stopTracking(taskId)", "stopTracking(taskId, context)")

content = content.replace("!hasLocationPermission()", "!hasLocationPermission(context)")
content = content.replace("!hasNotificationPermission()", "!hasNotificationPermission(context)")
content = content.replace("private fun hasLocationPermission(): Boolean", "private fun hasLocationPermission(context: android.content.Context): Boolean")
content = content.replace("private fun hasNotificationPermission(): Boolean", "private fun hasNotificationPermission(context: android.content.Context): Boolean")
content = content.replace("appContext", "context")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
