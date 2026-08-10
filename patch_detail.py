import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target1 = '''                            scope.launch {
                                try { 
                                    val claimRes = ApiClient.api.claimTask(taskId, mapOf("action" to "claim")) 
                                    if (claimRes.isSuccessful) {
                                        android.widget.Toast.makeText(context, "Task successfully claimed!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (_: Exception) {}
                                fetchTask()
                                claiming = false
                            }'''

replacement1 = '''                            scope.launch {
                                try { 
                                    val claimRes = ApiClient.api.claimTask(taskId, mapOf("action" to "claim")) 
                                    if (claimRes.isSuccessful) {
                                        snackbarHostState.showSnackbar("Task successfully claimed!")
                                    }
                                } catch (_: Exception) {}
                                fetchTask()
                                claiming = false
                            }'''

target2 = '''                    if (res.isSuccessful) {
                        Toast.makeText(context, "Proof submitted successfully", Toast.LENGTH_SHORT).show()
                    }'''

replacement2 = '''                    if (res.isSuccessful) {
                        snackbarHostState.showSnackbar("Proof submitted successfully")
                    }'''

if target1 in content or target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    
    if "val snackbarHostState" not in content:
        target_state = "    val context = LocalContext.current\n"
        replacement_state = "    val context = LocalContext.current\n    val snackbarHostState = com.strangerhelp.app.ui.components.LocalSnackbarHostState.current\n"
        content = content.replace(target_state, replacement_state)
    
    with open(path, 'w') as f:
        f.write(content)
    print("Detail patched.")
else:
    print("Detail target not found.")
