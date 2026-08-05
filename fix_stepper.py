import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''                if (t.description.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(t.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(24.dp))'''

replacement = target + '''
                
                com.strangerhelp.app.ui.components.TaskStatusStepper(t.status)
                
                Spacer(Modifier.height(16.dp))'''

content = content.replace(target, replacement)

with open(path, 'w') as f:
    f.write(content)
