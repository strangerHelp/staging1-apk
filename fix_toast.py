import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt", "r") as f:
    content = f.read()

content = content.replace("ApiClient.clearSession()", """
                                android.widget.Toast.makeText(navController.context, "Password reset successful. Please log in.", android.widget.Toast.LENGTH_LONG).show()
                                ApiClient.clearSession()
""")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt", "w") as f:
    f.write(content)
