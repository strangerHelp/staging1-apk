import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    text = f.read()

text = text.replace('composable("meets") { MeetsScreen(navController) }', '// removed MeetsScreen')

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(text)
