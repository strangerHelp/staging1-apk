import os

api_path = 'app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt'
with open(api_path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''    @GET("api/tasks/{id}")
    suspend fun getTask(@Path("id") id: String): Response<Task>'''
replacement = '''    @GET("api/tasks/{id}")
    suspend fun getTask(@Path("id") id: String, @Query("invite") inviteCode: String? = null): Response<Task>'''
content = content.replace(target, replacement)

with open(api_path, 'w', encoding='utf-8') as f:
    f.write(content)


task_path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'
with open(task_path, 'r', encoding='utf-8') as f:
    content2 = f.read()
    
target2 = '''                // Should append ?invite=... if needed, but Retrofit api definition is strict right now.
                // We'll just fetch normally for now.
                val res = ApiClient.api.getTask(taskId)'''
replacement2 = '''                val res = ApiClient.api.getTask(taskId, inviteCode)'''
content2 = content2.replace(target2, replacement2)

target3 = '''                try {
                    val res = ApiClient.api.getTask(taskId)
                    if (res.isSuccessful) task = res.body()
                } catch (_: Exception) {}'''
replacement3 = '''                try {
                    val res = ApiClient.api.getTask(taskId, inviteCode)
                    if (res.isSuccessful) task = res.body()
                } catch (_: Exception) {}'''
content2 = content2.replace(target3, replacement3)

with open(task_path, 'w', encoding='utf-8') as f:
    f.write(content2)

print("Updated getTask to support inviteCode")
