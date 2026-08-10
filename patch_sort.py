import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '                    item { FilterChipView("Distance", Icons.Filled.NearMe, isActive = false) { showFilters = true } }'
replacement = '''                    item { 
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            FilterChipView(
                                label = if (sortBy == "Nearest") "Sort" else sortBy, 
                                icon = Icons.Filled.Sort, 
                                isActive = sortBy != "Nearest"
                            ) { expanded = true }
                            
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("Nearest", "Highest Reward", "Most Recent").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            sortBy = option
                                            expanded = false
                                        },
                                        leadingIcon = {
                                            if (sortBy == option) {
                                                Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(20.dp))
                                            } else {
                                                Spacer(modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }'''

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched TasksScreen.kt with sort dropdown")
else:
    print("Target not found")
