with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace("        viewModel.startPosterPolling(taskId)\n", "")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
