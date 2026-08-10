import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

# 1. Add savedTasks state and savedFilter state (actually we just add savedFilter to the filter bar)
# `var urgentFilter by remember { mutableStateOf(false) }` is there.

target_states = '''    var urgentFilter by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Nearest") }'''

replacement_states = '''    var urgentFilter by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Nearest") }
    var savedFilter by remember { mutableStateOf(false) }
    val savedTasks = remember { mutableStateListOf<String>() }'''
content = content.replace(target_states, replacement_states)

# 2. Add 'Saved' to filter bar
target_filter_bar = '''                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { 
                        FilterChipView(
                            label = if (selectedCategory != "All") selectedCategory else "Category", 
                            icon = Icons.Filled.Category,
                            isActive = selectedCategory != "All"
                        ) { showFilters = true } 
                    }
                    item { FilterChipView("Budget", Icons.Filled.Payments, isActive = false) { showFilters = true } }
                    item { FilterChipView("Distance", Icons.Filled.NearMe, isActive = false) { showFilters = true } }
                }'''

replacement_filter_bar = '''                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { 
                        FilterChipView(
                            label = if (selectedCategory != "All") selectedCategory else "Category", 
                            icon = Icons.Filled.Category,
                            isActive = selectedCategory != "All"
                        ) { showFilters = true } 
                    }
                    item { FilterChipView("Saved", if (savedFilter) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, isActive = savedFilter) { savedFilter = !savedFilter } }
                    item { FilterChipView("Budget", Icons.Filled.Payments, isActive = false) { showFilters = true } }
                    item { FilterChipView("Distance", Icons.Filled.NearMe, isActive = false) { showFilters = true } }
                }'''
content = content.replace(target_filter_bar, replacement_filter_bar)

# 3. Apply savedFilter
target_sorting = '''                val sortedTasks = remember(tasks, sortBy) {
                    when (sortBy) {
                        "Nearest" -> tasks.sortedBy { it.distance ?: Double.MAX_VALUE }
                        "Highest Reward" -> tasks.sortedByDescending { it.budget }
                        "Most Recent" -> tasks.sortedByDescending { it.createdAt }
                        else -> tasks
                    }
                }'''

replacement_sorting = '''                val sortedTasks = remember(tasks, sortBy, savedFilter, savedTasks.size) {
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
                }'''
content = content.replace(target_sorting, replacement_sorting)

# 4. Modify TaskCard parameter
target_taskcard_def = '''fun TaskCard(task: Task, onClick: () -> Unit) {'''
replacement_taskcard_def = '''fun TaskCard(task: Task, isSaved: Boolean, onToggleSave: () -> Unit, onClick: () -> Unit) {'''
content = content.replace(target_taskcard_def, replacement_taskcard_def)

# 5. Modify TaskCard usage
target_taskcard_usage = '''TaskCard(task = task, onClick = { navController.navigate("task/${task._id}") })'''
replacement_taskcard_usage = '''TaskCard(
                            task = task, 
                            isSaved = savedTasks.contains(task._id), 
                            onToggleSave = { 
                                if (savedTasks.contains(task._id)) savedTasks.remove(task._id) 
                                else savedTasks.add(task._id) 
                            }, 
                            onClick = { navController.navigate("task/${task._id}") }
                        )'''
content = content.replace(target_taskcard_usage, replacement_taskcard_usage)

# 6. Add save button to TaskCard
target_header = '''                // Badge (Verified / Urgent)
                if (task.urgent == 1) {'''
replacement_header = '''                // Badge (Verified / Urgent)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (task.urgent == 1) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                Text("URGENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else if (task.posterVerified) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.Verified, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text("VERIFIED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                    IconButton(onClick = onToggleSave, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Save Task",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }'''

target_old_badge_part = '''                // Badge (Verified / Urgent)
                if (task.urgent == 1) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                            Text("URGENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else if (task.posterVerified) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Verified, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                            Text("VERIFIED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }'''

content = content.replace(target_old_badge_part, replacement_header)


# Check and add import for bookmark if not there
if "Icons.Filled.Bookmark" not in content and "import androidx.compose.material.icons.filled.Bookmark" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.Bookmark\nimport androidx.compose.material.icons.filled.BookmarkBorder")

with open(path, 'w') as f:
    f.write(content)

