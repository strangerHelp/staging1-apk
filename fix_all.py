import sys

# 1. PosterComponents.kt
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

content = content.replace("import com.strangerhelp.app.ui.components.MapView\n", "")
content = content.replace("OutlinedButtonDefaults.outlinedButtonColors", "ButtonDefaults.outlinedButtonColors")
content = content.replace("TextButtonDefaults.textButtonColors", "ButtonDefaults.textButtonColors")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)

# 2. TaskActionSection.kt Icons
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.automirrored.filled.Chat", "androidx.compose.material.icons.Icons.AutoMirrored.Filled.Chat")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(content)

# 3. TaskDetailScreen.kt Icons
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.automirrored.filled.Chat", "androidx.compose.material.icons.Icons.AutoMirrored.Filled.Chat")
content = content.replace("androidx.compose.material.icons.automirrored.outlined.Chat", "androidx.compose.material.icons.Icons.AutoMirrored.Outlined.Chat")
content = content.replace("androidx.compose.material.icons.automirrored.filled.ArrowBack", "androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)

