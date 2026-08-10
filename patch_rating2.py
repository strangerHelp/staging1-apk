import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Outlined.Star", "androidx.compose.material.icons.Icons.Outlined.Star")
content = content.replace("androidx.compose.material.icons.Icons.Filled.Star", "androidx.compose.material.icons.Icons.Filled.Star")

# Let's just remove the explicit package name and add imports
content = content.replace("androidx.compose.material.icons.Icons.Outlined.Star", "Icons.Outlined.Star")
content = content.replace("androidx.compose.material.icons.Icons.Filled.Star", "Icons.Filled.Star")

if "import androidx.compose.material.icons.filled.Star" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.Star")
if "import androidx.compose.material.icons.outlined.Star" not in content:
    content = content.replace("import androidx.compose.material.icons.outlined.*", "import androidx.compose.material.icons.outlined.*\nimport androidx.compose.material.icons.outlined.Star")

with open(path, 'w') as f:
    f.write(content)
