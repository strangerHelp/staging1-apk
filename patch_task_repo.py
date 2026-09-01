import re

file_path = "app/src/main/java/com/strangerhelp/app/data/repository/TaskRepository.kt"
with open(file_path, "r") as f:
    content = f.read()

new_method = """    suspend fun getMyTasks(filter: String = "all"): Response<List<Task>> {
        val queryMap = mutableMapOf(
            "mine" to "true",
            "limit" to "50"
        )
        if (filter != "all") {
            queryMap["role"] = filter
        }
        return api.getTasksWithQueryMap(queryMap)
    }
"""

if "getMyTasks" not in content:
    content = content.replace('class TaskRepository(private val api: StrangerHelpApi) {', 'class TaskRepository(private val api: StrangerHelpApi) {\n' + new_method)

with open(file_path, "w") as f:
    f.write(content)
