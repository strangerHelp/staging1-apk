import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    text = f.read()

text = text.replace("""task.value?._id ?: task.value?.id ?: \"\"""", """task.value?._id ?: \"\"""")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(text)
