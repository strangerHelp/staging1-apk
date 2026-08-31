import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# Add isOffline variable collection
if "val isOffline by viewModel.isOffline" not in content:
    content = content.replace(
        "val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()",
        "val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()\n    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()"
    )

# Add AnimatedVisibility to imports
if "import androidx.compose.animation.AnimatedVisibility" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.animation.AnimatedVisibility\nimport androidx.compose.foundation.layout.*")

# Replace the LazyColumn wrapper area
# We want to put a Column wrapping the LazyColumn.
# Current:
#         } else {
#             task?.let { t ->
#                 LazyColumn(
#                     modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFFAFAFA)),
#                     verticalArrangement = Arrangement.spacedBy(16.dp),
#                     contentPadding = PaddingValues(16.dp)
#                 ) {
#                     // 1. Task Info Card

# Replacement:
replacement = """        } else {
            task?.let { t ->
                Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFFAFAFA))) {
                    AnimatedVisibility(visible = isOffline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reconnecting... showing stale data",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {"""

content = re.sub(
    r'        \} else \{\n            task\?\.let \{ t ->\n                LazyColumn\(\n                    modifier = Modifier\.fillMaxSize\(\)\.padding\(padding\)\.background\(Color\(0xFFFAFAFA\)\),\n                    verticalArrangement = Arrangement\.spacedBy\(16\.dp\),\n                    contentPadding = PaddingValues\(16\.dp\)\n                \) \{',
    replacement,
    content
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)
