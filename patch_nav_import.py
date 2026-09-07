import re

file_path = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace("import com.strangerhelp.app.ui.screens.ask.AskScreen\n", "")

with open(file_path, "w") as f:
    f.write(content)
