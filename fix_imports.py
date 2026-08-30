import os

files_to_fix = [
    'app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt',
    'app/src/main/java/com/strangerhelp/app/ui/screens/path/PathActiveScreen.kt'
]

for path in files_to_fix:
    with open(path, 'r') as f:
        content = f.read()
    
    if 'import androidx.compose.foundation.BorderStroke' not in content:
        content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport androidx.compose.foundation.BorderStroke')
        with open(path, 'w') as f:
            f.write(content)
        print(f"Fixed {path}")
