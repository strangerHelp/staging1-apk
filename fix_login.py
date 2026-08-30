import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("val context = LocalContext.current\n    val oauthLauncher", "val oauthLauncher")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt', 'w') as f:
    f.write(content)
