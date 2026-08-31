import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Icons.Default.Chat", "androidx.compose.material.icons.automirrored.filled.Chat")
content = content.replace("Icons.Filled.Chat", "androidx.compose.material.icons.automirrored.filled.Chat")
content = content.replace("Icons.Outlined.Chat", "androidx.compose.material.icons.automirrored.outlined.Chat")
content = content.replace("Icons.Default.ArrowBack", "androidx.compose.material.icons.automirrored.filled.ArrowBack")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
