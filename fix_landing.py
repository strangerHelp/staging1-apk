import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.runtime.Composable', 'import androidx.compose.runtime.Composable\nimport androidx.compose.foundation.BorderStroke')

with open(path, 'w') as f:
    f.write(content)
print(f"Fixed {path}")
