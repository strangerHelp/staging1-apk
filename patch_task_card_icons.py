import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('Icons.Outlined.Schedule', 'Icons.Filled.Schedule')
content = content.replace('Icons.Outlined.LocationOn', 'Icons.Filled.LocationOn')

with open(path, 'w') as f:
    f.write(content)
print("Patched Icons")
