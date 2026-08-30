with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ComingSoonScreens.kt', 'r') as f:
    content = f.read()

content = content.replace("unfocusedBorderColor = MaterialTheme.colorScheme.outline),\n                    focusedBorderColor", "unfocusedBorderColor = MaterialTheme.colorScheme.outline,\n                    focusedBorderColor")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/profile/ComingSoonScreens.kt', 'w') as f:
    f.write(content)
