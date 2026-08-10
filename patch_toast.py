import os

path_tasks = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path_tasks, 'r') as f:
    content_tasks = f.read()

target_tasks = '''                            onToggleSave = { 
                                if (savedTasks.contains(task._id)) savedTasks.remove(task._id) 
                                else savedTasks.add(task._id) 
                                prefs.edit().putStringSet("saved_tasks", savedTasks.toSet()).apply()
                            },'''

replacement_tasks = '''                            onToggleSave = { 
                                if (savedTasks.contains(task._id)) {
                                    savedTasks.remove(task._id) 
                                    android.widget.Toast.makeText(context, "Task removed from saved", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    savedTasks.add(task._id) 
                                    android.widget.Toast.makeText(context, "Task saved!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                prefs.edit().putStringSet("saved_tasks", savedTasks.toSet()).apply()
                            },'''

if target_tasks in content_tasks:
    content_tasks = content_tasks.replace(target_tasks, replacement_tasks)
    with open(path_tasks, 'w') as f:
        f.write(content_tasks)
    print("Tasks patched.")
else:
    print("Tasks target not found.")

path_detail = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'
with open(path_detail, 'r') as f:
    content_detail = f.read()

target_detail = '''                            scope.launch {
                                try { ApiClient.api.claimTask(taskId, mapOf("action" to "claim")) } catch (_: Exception) {}
                                fetchTask()
                                claiming = false
                            }'''

replacement_detail = '''                            scope.launch {
                                try { 
                                    val claimRes = ApiClient.api.claimTask(taskId, mapOf("action" to "claim")) 
                                    if (claimRes.isSuccessful) {
                                        android.widget.Toast.makeText(context, "Task successfully claimed!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (_: Exception) {}
                                fetchTask()
                                claiming = false
                            }'''

if target_detail in content_detail:
    content_detail = content_detail.replace(target_detail, replacement_detail)
    with open(path_detail, 'w') as f:
        f.write(content_detail)
    print("Detail patched.")
else:
    print("Detail target not found.")

