import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = 'val context = LocalContext.current'
replacement = 'val context = LocalContext.current\n    val snackbarHostState = com.strangerhelp.app.ui.components.LocalSnackbarHostState.current'

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Detail patched again.")
