import re

with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

if "WebViewScreen" not in content[:1000]:
    print("WebViewScreen NOT in imports of AppNavigation.kt")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.ui.graphics.Color" not in content:
    print("Color NOT in imports of PostTaskScreen.kt")

