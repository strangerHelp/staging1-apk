with open("app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt", "r") as f:
    content = f.read()

content = content.replace("darkTheme: Boolean = false, // Forced white/light theme", "darkTheme: Boolean = isSystemInDarkTheme(),")

with open("app/src/main/java/com/strangerhelp/app/ui/theme/Theme.kt", "w") as f:
    f.write(content)
