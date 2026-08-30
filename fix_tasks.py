with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("val MaterialTheme.colorScheme.outline = Color(0xFFEBEBEB)\n", "")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt', 'w') as f:
    f.write(content)
