import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.*")
content = content.replace("Icons.AutoMirrored.Filled.ArrowBack", "Icons.Default.ArrowBack")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraScreen.kt", "w") as f:
    f.write(content)
