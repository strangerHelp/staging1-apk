import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

old_code = """                val payload = updates.toMutableMap()
                payload["action"] = "edit"
                val response = taskRepository.patchTask(taskId, payload)
                if (response.isSuccessful) {
                    loadTask(taskId)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }"""

new_code = """                val payload = updates.toMutableMap()
                payload["action"] = "edit"
                val response = taskRepository.patchTask(taskId, payload)
                if (response.isSuccessful) {
                    loadTask(taskId)
                    onResult(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = parseError(errorBody)
                    onResult(false)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to edit task"
                onResult(false)
            }"""

content = content.replace(old_code, new_code)

with open(file_path, "w") as f:
    f.write(content)
