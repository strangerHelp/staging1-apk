import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Filled.Chat", "androidx.compose.material.icons.automirrored.filled.Chat")
content = content.replace("Icons.Default.Chat", "androidx.compose.material.icons.automirrored.filled.Chat")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)
