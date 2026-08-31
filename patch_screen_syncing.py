import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# Add isSyncing variable
if "val isSyncing by viewModel.isSyncing" not in content:
    content = content.replace(
        "val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()",
        "val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()\n    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()"
    )

# Add indicator in TopAppBar actions
indicator_code = """                actions = {
                    if (isSyncing) {
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
                    var showMenu by remember { mutableStateOf(false) }"""

content = content.replace("""                actions = {
                    var showMenu by remember { mutableStateOf(false) }""", indicator_code)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
