import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

var_target = '    var showFilters by remember { mutableStateOf(false) }'
var_replacement = '''    var showFilters by remember { mutableStateOf(false) }
    var showNewTaskSheet by remember { mutableStateOf(false) }'''

box_target = '''        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }'''
box_replacement = '''        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        FloatingActionButton(
            onClick = { showNewTaskSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New Task")
        }
    }'''

sheet_target = '''    if (showFilters) {'''
sheet_replacement = '''    if (showNewTaskSheet) {
        var newTaskTitle by remember { mutableStateOf("") }
        var newTaskReward by remember { mutableStateOf("") }
        var newTaskLocation by remember { mutableStateOf("") }
        var isPosting by remember { mutableStateOf(false) }
        
        ModalBottomSheet(onDismissRequest = { showNewTaskSheet = false }) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Post a New Task", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newTaskReward,
                    onValueChange = { newTaskReward = it },
                    label = { Text("Reward (₹)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newTaskLocation,
                    onValueChange = { newTaskLocation = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isPosting = true
                            delay(1000)
                            isPosting = false
                            showNewTaskSheet = false
                            snackbarHostState.showSnackbar("Task posted successfully!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = newTaskTitle.isNotBlank() && newTaskReward.isNotBlank() && newTaskLocation.isNotBlank() && !isPosting
                ) {
                    if (isPosting) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Post Task")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showFilters) {'''

if var_target in content and box_target in content and sheet_target in content:
    content = content.replace(var_target, var_replacement)
    content = content.replace(box_target, box_replacement)
    content = content.replace(sheet_target, sheet_replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched TasksScreen.kt with FAB and bottom sheet.")
else:
    print("Targets not found.")
    if var_target not in content:
        print("var_target missing")
    if box_target not in content:
        print("box_target missing")
    if sheet_target not in content:
        print("sheet_target missing")
