import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace("val reviewViewModel: ReviewViewModel = viewModel()", "val reviewViewModel: ReviewViewModel = viewModel { ReviewViewModel() }")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
