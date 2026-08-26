import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

# Replace TaskDetailScreen signature
old_sig = """fun TaskDetailScreen(
    navController: NavController,
    taskId: String,
    viewModel: TaskDetailViewModel = viewModel(factory = TaskDetailViewModelFactory())
)"""

new_sig = """fun TaskDetailScreen(
    navController: NavController,
    user: User?,
    taskId: String,
    viewModel: TaskDetailViewModel = viewModel(factory = TaskDetailViewModelFactory())
)"""

content = content.replace(old_sig, new_sig)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(content)
