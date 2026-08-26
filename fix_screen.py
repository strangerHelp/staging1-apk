import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

# Fix ButtonDefaults
content = content.replace("OutlinedButtonDefaults.outlinedButtonColors(", "ButtonDefaults.outlinedButtonColors(")
content = content.replace("TextButtonDefaults.textButtonColors(", "ButtonDefaults.textButtonColors(")

# Remove snackbar code
content = re.sub(r'if \(error != null\) \{[\s\S]*?viewModel\.clearError\(\)\n\s*\}\n\s*\}', '', content)

# Add factory
factory_code = """
class TaskDetailViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return TaskDetailViewModel() as T
    }
}
"""
content = content + "\n" + factory_code

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(content)
