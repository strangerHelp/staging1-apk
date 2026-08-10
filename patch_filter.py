import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''                    item { 
                        FilterChip(
                            selected = savedFilter,
                            onClick = { savedFilter = !savedFilter },
                            label = { Text("Saved") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (savedFilter) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Saved tasks",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }'''

replacement = '''                    item { 
                        FilterChip(
                            selected = savedFilter,
                            onClick = { savedFilter = !savedFilter },
                            label = { Text(if (savedFilter) "Bookmarked" else "All Tasks") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (savedFilter) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Saved tasks",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }'''

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched FilterChip")
else:
    print("Target not found")
