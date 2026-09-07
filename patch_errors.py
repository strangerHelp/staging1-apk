import re

file_path = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(file_path, "r") as f:
    content = f.read()

# Fix AuthRepository instantiation in askViewModel
content = content.replace("com.strangerhelp.app.data.repository.AuthRepository(com.strangerhelp.app.data.api.ApiClient.api, sharedPrefs)", 
                          "com.strangerhelp.app.data.repository.AuthRepository(com.strangerhelp.app.data.api.ApiClient.api)")

with open(file_path, "w") as f:
    f.write(content)


file_path2 = "app/src/main/java/com/strangerhelp/app/ui/screens/ask/AskScreens.kt"
with open(file_path2, "r") as f:
    content2 = f.read()

content2 = content2.replace("import com.strangerhelp.app.ui.components.CategoryChip\n", "")
content2 = content2.replace("import com.strangerhelp.app.ui.components.ErrorCard\n", "")
# Fix Divider to HorizontalDivider
content2 = content2.replace("Divider(", "HorizontalDivider(")

with open(file_path2, "w") as f:
    f.write(content2)

