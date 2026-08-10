import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target_states = '''    var urgentFilter by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Nearest") }
    var savedFilter by remember { mutableStateOf(false) }
    val savedTasks = remember { mutableStateListOf<String>() }'''

replacement_states = '''    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("strangerhelp_prefs", android.content.Context.MODE_PRIVATE) }
    var urgentFilter by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Nearest") }
    var savedFilter by remember { mutableStateOf(false) }
    val savedTasks = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("saved_tasks", emptySet()) ?: emptySet()) } }'''
content = content.replace(target_states, replacement_states)

target_taskcard_usage = '''TaskCard(
                            task = task, 
                            isSaved = savedTasks.contains(task._id), 
                            onToggleSave = { 
                                if (savedTasks.contains(task._id)) savedTasks.remove(task._id) 
                                else savedTasks.add(task._id) 
                            }, 
                            onClick = { navController.navigate("task/${task._id}") }
                        )'''

replacement_taskcard_usage = '''TaskCard(
                            task = task, 
                            isSaved = savedTasks.contains(task._id), 
                            onToggleSave = { 
                                if (savedTasks.contains(task._id)) savedTasks.remove(task._id) 
                                else savedTasks.add(task._id) 
                                prefs.edit().putStringSet("saved_tasks", savedTasks.toSet()).apply()
                            }, 
                            onClick = { navController.navigate("task/${task._id}") }
                        )'''
content = content.replace(target_taskcard_usage, replacement_taskcard_usage)

with open(path, 'w') as f:
    f.write(content)

