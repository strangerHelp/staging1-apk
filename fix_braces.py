with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("    private var pollJob: Job? = null\n\n            }\n        }\n    }\n", "    private var pollJob: Job? = null\n")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
