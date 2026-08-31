import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ForgotPasswordScreen.kt", "r") as f:
    content = f.read()

replacement = """
                        try {
                            val res = ApiClient.api.forgotPassword(mapOf("email" to email.trim()))
                            if (res.isSuccessful) {
                                navController.navigate("email_sent/${email.trim()}")
                            } else {
                                val errorStr = res.errorBody()?.string() ?: ""
                                error = if (res.code() == 429) {
                                    "Too many requests. Please wait before trying again."
                                } else {
                                    "Failed to send reset link. Please try again."
                                }
                            }
                        } catch (e: Exception) {
                            error = "Network error. Please check your connection."
                        }
"""

content = re.sub(
    r'try\s*\{\s*val res = ApiClient\.api\.forgotPassword[\s\S]*?\} catch \(e: Exception\)\s*\{\s*error = "Network error\. Please check your connection\."\s*\}',
    replacement.strip(),
    content
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/auth/ForgotPasswordScreen.kt", "w") as f:
    f.write(content)
