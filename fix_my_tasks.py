import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/tasks/MyTasksScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace('import androidx.lifecycle.compose.collectAsStateWithLifecycle', 'import androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.height')

with open(file_path, "w") as f:
    f.write(content)
