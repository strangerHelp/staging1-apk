import sys
import glob

files = glob.glob("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/*.kt")

for file in files:
    with open(file, "r") as f:
        content = f.read()
    
    content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Filled.Chat", "Icons.Default.Chat")
    content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Outlined.Chat", "Icons.Outlined.Chat")
    content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack", "Icons.Default.ArrowBack")
    content = content.replace("Icons.AutoMirrored.Filled.ArrowBack", "Icons.Default.ArrowBack")
    content = content.replace("Icons.AutoMirrored.Filled.Chat", "Icons.Default.Chat")
    
    with open(file, "w") as f:
        f.write(content)

