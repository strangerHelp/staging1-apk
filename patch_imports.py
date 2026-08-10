import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = "import androidx.compose.material.icons.automirrored.filled.ArrowBack"
replacement = "import androidx.compose.material.icons.automirrored.filled.ArrowBack\nimport androidx.compose.material.icons.filled.Star\nimport androidx.compose.material.icons.outlined.Star"
content = content.replace(target, replacement)

with open(path, 'w') as f:
    f.write(content)
