import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/notifications/NotificationsScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

old_content = """        } else if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No notifications yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "We'll notify you when something happens",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }"""

new_content = """        } else if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                com.strangerhelp.app.ui.components.EmptyState(
                    icon = "🔔",
                    title = "No notifications yet",
                    message = "We'll notify you when something happens"
                )
            }
        }"""

if "Column(horizontalAlignment = Alignment.CenterHorizontally)" in content:
    content = content.replace(old_content, new_content)

with open(file_path, "w") as f:
    f.write(content)
