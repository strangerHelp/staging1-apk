sed -i '/Spacer(Modifier.height(24.dp))/a \
                com.strangerhelp.app.ui.components.TaskStatusStepper(t.status)\n                Spacer(Modifier.height(24.dp))' app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt
