import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target_chip = '''@Composable
fun FilterChipView(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}'''

replacement_chip = '''@Composable
fun FilterChipView(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}'''

content = content.replace(target_chip, replacement_chip)

target_row = '''            // Filter Section
            Surface(color = MaterialTheme.colorScheme.surface) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { FilterChipView("Category", Icons.Filled.Category) { showFilters = true } }
                    item { FilterChipView("Budget", Icons.Filled.Payments) { showFilters = true } }
                    item { FilterChipView("Distance", Icons.Filled.NearMe) { showFilters = true } }
                }
            }'''

replacement_row = '''            // Filter Section
            Surface(color = MaterialTheme.colorScheme.surface) {
                LazyRow(
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
                }
            }'''

content = content.replace(target_row, replacement_row)

target_filters = '''    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Urgent Tasks Only", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = urgentFilter, onCheckedChange = { urgentFilter = it })
                }
                
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showFilters = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Apply Filters")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }'''

replacement_filters = '''    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Urgent Tasks Only", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = urgentFilter, onCheckedChange = { urgentFilter = it })
                }
                
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showFilters = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Apply Filters")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }'''

content = content.replace(target_filters, replacement_filters)

with open(path, 'w') as f:
    f.write(content)

