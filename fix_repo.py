import re

with open('app/src/main/java/com/strangerhelp/app/data/repository/TaskRepository.kt', 'r') as f:
    content = f.read()

delete_task_code = """
    suspend fun deleteTask(taskId: String) = api.deleteTask(taskId)
"""

content = content.replace("}", delete_task_code + "}")

with open('app/src/main/java/com/strangerhelp/app/data/repository/TaskRepository.kt', 'w') as f:
    f.write(content)
