import re

file_path = "app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt"
with open(file_path, "r") as f:
    content = f.read()

new_method = """    @GET("api/tasks")
    suspend fun getTasksWithQueryMap(@QueryMap queryMap: Map<String, String>): Response<List<Task>>
"""

if "getTasksWithQueryMap" not in content:
    content = content.replace('): Response<List<Task>>\n', '): Response<List<Task>>\n\n' + new_method)
    content = content.replace('import retrofit2.http.Query\n', 'import retrofit2.http.Query\nimport retrofit2.http.QueryMap\n')

with open(file_path, "w") as f:
    f.write(content)
