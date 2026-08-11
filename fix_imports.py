import os

for path in ['app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt', 'app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt']:
    with open(path, 'r') as f:
        content = f.read()
    
    content = content.replace('import withStyle', 'import androidx.compose.ui.text.withStyle')
    
    with open(path, 'w') as f:
        f.write(content)
