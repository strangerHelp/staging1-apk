import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# I will add reviewViewModel inside the function body
content = content.replace("    val task by viewModel.task.collectAsStateWithLifecycle()", "    val reviewViewModel: ReviewViewModel = viewModel()\n    val task by viewModel.task.collectAsStateWithLifecycle()")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
