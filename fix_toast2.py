import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt", "r") as f:
    content = f.read()

# I will add `val context = androidx.compose.ui.platform.LocalContext.current` at the top of the Composable
content = content.replace("val scope = rememberCoroutineScope()", "val scope = rememberCoroutineScope()\n    val context = androidx.compose.ui.platform.LocalContext.current")

# Update toast
content = content.replace("navController.context", "context")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt", "w") as f:
    f.write(content)
