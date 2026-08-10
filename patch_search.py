import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

import_target = 'import com.strangerhelp.app.ui.components.shimmerEffect'
import_replacement = '''import com.strangerhelp.app.ui.components.shimmerEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strangerhelp.app.StrangerHelpApp
import androidx.lifecycle.compose.collectAsStateWithLifecycle'''

vm_target = '@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun TasksScreen(navController: NavController) {'
vm_replacement = '''@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = (context.applicationContext as StrangerHelpApp).database
    val viewModel: TasksViewModel = viewModel(factory = TasksViewModelFactory(database.searchHistoryDao()))
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    var searchExpanded by remember { mutableStateOf(false) }'''

# Let's fix the first occurence of context in TasksScreen
ctx_target = '    val context = androidx.compose.ui.platform.LocalContext.current\n    val snackbarHostState = com.strangerhelp.app.ui.components.LocalSnackbarHostState.current'
ctx_replacement = '    val snackbarHostState = com.strangerhelp.app.ui.components.LocalSnackbarHostState.current'

# We'll save the search string on submit or when fetching tasks with a new search query.
# Let's add it in fetchTasks when a search is done.
search_fetch_target = '''                val cat = if (selectedCategory == "All") null else selectedCategory
                val q = debouncedQuery.takeIf { it.isNotBlank() }
                val urgentStr = if (urgentFilter) "1" else null'''
search_fetch_replacement = '''                val cat = if (selectedCategory == "All") null else selectedCategory
                val q = debouncedQuery.takeIf { it.isNotBlank() }
                if (q != null) viewModel.saveSearch(q)
                val urgentStr = if (urgentFilter) "1" else null'''

search_section_target = '''            // Search Section
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tasks, locations...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }'''
search_section_replacement = '''            // Search Section
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                ExposedDropdownMenuBox(
                    expanded = searchExpanded,
                    onExpandedChange = { 
                        if (recentSearches.isNotEmpty()) searchExpanded = it 
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            searchExpanded = recentSearches.isNotEmpty()
                        },
                        placeholder = { Text("Search tasks, locations...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchQuery = "" 
                                    searchExpanded = recentSearches.isNotEmpty()
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    
                    if (recentSearches.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = searchExpanded,
                            onDismissRequest = { searchExpanded = false }
                        ) {
                            Text("Recent Searches", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            recentSearches.forEach { history ->
                                DropdownMenuItem(
                                    text = { Text(history.query) },
                                    onClick = {
                                        searchQuery = history.query
                                        searchExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Filled.History, contentDescription = "History") },
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.deleteSearch(history.query) }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Remove")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }'''

if import_target in content:
    content = content.replace(import_target, import_replacement)
    content = content.replace(vm_target, vm_replacement)
    content = content.replace(ctx_target, ctx_replacement)
    content = content.replace(search_fetch_target, search_fetch_replacement)
    content = content.replace(search_section_target, search_section_replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched Search History")
else:
    print("import target not found")
