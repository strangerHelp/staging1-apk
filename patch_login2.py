import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt', 'r') as f:
    content = f.read()

# Update function signature
content = content.replace(
    "fun LoginScreen(onLoginSuccess: (User) -> Unit, onForgotPasswordClick: () -> Unit = {}) {",
    "fun LoginScreen(onLoginSuccess: (User) -> Unit, onForgotPasswordClick: () -> Unit = {}, onGoogleLoginClick: () -> Unit = {}) {"
)

# Update onClick handler
old_click = """onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://strangerhelp.com/api/auth/google"))
                        context.startActivity(intent)
                    }"""
new_click = "onClick = onGoogleLoginClick"
content = content.replace(old_click, new_click)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/auth/LoginScreen.kt', 'w') as f:
    f.write(content)
print("Patched LoginScreen.kt")
