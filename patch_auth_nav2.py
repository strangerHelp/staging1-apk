import re

with open('app/src/main/java/com/strangerhelp/app/navigation/AuthNavigation.kt', 'r') as f:
    content = f.read()

content = content.replace(
    """        composable("login") {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }""",
    """        composable("login") {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                onGoogleLoginClick = { navController.navigate("oauth_webview") }
            )
        }"""
)

with open('app/src/main/java/com/strangerhelp/app/navigation/AuthNavigation.kt', 'w') as f:
    f.write(content)
print("Patched AuthNavigation.kt again")
