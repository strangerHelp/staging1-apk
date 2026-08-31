import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt", "r") as f:
    content = f.read()

content = content.replace('success = true', '''
                                android.widget.Toast.makeText(androidx.compose.ui.platform.LocalContext.current, "Password reset successful. Please log in.", android.widget.Toast.LENGTH_LONG).show()
                                ApiClient.clearSession()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
''')

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt", "w") as f:
    f.write(content)
