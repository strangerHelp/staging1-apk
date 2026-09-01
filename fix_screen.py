import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.hilt.navigation.compose.hiltViewModel", "import androidx.lifecycle.viewmodel.compose.viewModel")
content = content.replace("reviewViewModel: ReviewViewModel = hiltViewModel()", "reviewViewModel: ReviewViewModel = viewModel()")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
