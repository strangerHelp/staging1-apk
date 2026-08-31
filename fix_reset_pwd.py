import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt", "r") as f:
    content = f.read()

# Update password minimum validation to 10
content = content.replace("password.length < 8", "password.length < 10")
content = content.replace("At least 8 characters", "Min 10 characters")
content = content.replace("Must be at least 8 characters", "Must be at least 10 characters")

# Ensure ApiClient.clearSession() is called on success
replacement = """
                        try {
                            val res = ApiClient.api.resetPassword(mapOf("token" to token, "password" to password))
                            if (res.isSuccessful) {
                                ApiClient.clearSession()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            } else {
                                val errorStr = res.errorBody()?.string() ?: ""
                                error = if (res.code() == 429) {
                                    "Too many requests. Please wait before trying again."
                                } else if (res.code() == 400 && errorStr.contains("expired", ignoreCase = true)) {
                                    "Reset link has expired. Please request a new one."
                                } else {
                                    "Failed to reset password. Please try again."
                                }
                            }
                        } catch (e: Exception) {
                            error = "Network error. Please check your connection."
                        }
"""

content = re.sub(
    r'try\s*\{\s*val res = ApiClient\.api\.resetPassword[\s\S]*?\} catch \(e: Exception\)\s*\{\s*error = "Network error\. Please check your connection\."\s*\}',
    replacement.strip(),
    content
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt", "w") as f:
    f.write(content)
