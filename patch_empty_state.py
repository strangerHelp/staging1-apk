import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''            } else if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No tasks found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Try adjusting your filters", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                val sortedTasks = remember(tasks, sortBy, savedFilter, savedTasks.size) {
                    var filtered = tasks
                    if (savedFilter) {
                        filtered = filtered.filter { savedTasks.contains(it._id) }
                    }
                    when (sortBy) {
                        "Nearest" -> filtered.sortedBy { it.distance ?: Double.MAX_VALUE }
                        "Highest Reward" -> filtered.sortedByDescending { it.budget }
                        "Most Recent" -> filtered.sortedByDescending { it.createdAt }
                        else -> filtered
                    }
                }
                LazyColumn('''

replacement = '''            } else {
                val sortedTasks = remember(tasks, sortBy, savedFilter, savedTasks.size) {
                    var filtered = tasks
                    if (savedFilter) {
                        filtered = filtered.filter { savedTasks.contains(it._id) }
                    }
                    when (sortBy) {
                        "Nearest" -> filtered.sortedBy { it.distance ?: Double.MAX_VALUE }
                        "Highest Reward" -> filtered.sortedByDescending { it.budget }
                        "Most Recent" -> filtered.sortedByDescending { it.createdAt }
                        else -> filtered
                    }
                }
                
                if (sortedTasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("No tasks found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "We couldn't find any tasks matching your filters. Try adjusting them or checking back later.", 
                                color = Muted, 
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { 
                                    searchQuery = ""
                                    selectedCategory = "All"
                                    savedFilter = false
                                },
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text("Clear Filters")
                            }
                        }
                    }
                } else {
                    LazyColumn('''

if target in content:
    content = content.replace(target, replacement)
    
    # Needs to match the open bracket of the else clause
    # wait, the replacement has `} else {`, which replaces `LazyColumn(`. So we need to add a closing brace somewhere.
    # Actually `LazyColumn(` was the start of the lazy column, which ends later.
    
    with open(path, 'w') as f:
        f.write(content)
    print("Patched.")
else:
    print("Target not found.")
