import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# 1. Remove the old isSyncing logic from TopAppBar actions
old_sync_logic = """                    if (isSyncing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Syncing...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
"""
content = content.replace(old_sync_logic, "")

# 2. Add LinearProgressIndicator and/or syncing banner above the LazyColumn
banner_logic = """                Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFFAFAFA))) {
                    AnimatedVisibility(visible = isSyncing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    AnimatedVisibility(visible = isOffline) {"""

content = content.replace("""                Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFFAFAFA))) {
                    AnimatedVisibility(visible = isOffline) {""", banner_logic)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
