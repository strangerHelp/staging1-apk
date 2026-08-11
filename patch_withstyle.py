import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.ui.text.withStyle', 'withStyle')
with open(path, 'w') as f:
    f.write(content)

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt'
with open(path, 'r') as f:
    content = f.read()
content = content.replace('androidx.compose.ui.text.withStyle', 'withStyle')
with open(path, 'w') as f:
    f.write(content)
