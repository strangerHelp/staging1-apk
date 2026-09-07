import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/ask/AskScreens.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace("import androidx.hilt.navigation.compose.hiltViewModel\n", "")
content = content.replace("viewModel: AskViewModel = hiltViewModel(),", "viewModel: AskViewModel,")

with open(file_path, "w") as f:
    f.write(content)
