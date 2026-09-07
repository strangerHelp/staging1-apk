import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/ask/AskScreens.kt"
with open(file_path, "r") as f:
    content = f.read()

# Fix import
content = content.replace("import com.strangerhelp.app.ui.screens.auth.LoginPrompt", 
                          "import com.strangerhelp.app.ui.screens.tasks.LoginPrompt")

# Fix usage
old_usage = """                            LoginPrompt(
                                message = "Login to answer this question",
                                onLoginClick = {
                                    navController.navigate("login")
                                }
                            )"""

new_usage = """                            LoginPrompt(navController = navController)"""

content = content.replace(old_usage, new_usage)

with open(file_path, "w") as f:
    f.write(content)
