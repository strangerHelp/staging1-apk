import re

file_path = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add the 'meets' composable
if 'composable("meets")' not in content:
    content = content.replace(
        'composable("path_setup")',
        'composable("meets") { com.strangerhelp.app.ui.screens.meets.MeetsListScreen(meetViewModel, navController) }\n                composable("path_setup")'
    )

with open(file_path, "w") as f:
    f.write(content)
