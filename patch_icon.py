import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('Icons.Outlined.EditDocument', 'Icons.Outlined.Edit')

with open(path, 'w') as f:
    f.write(content)
