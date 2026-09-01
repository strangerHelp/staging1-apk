import re

file_path_api = "app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt"
with open(file_path_api, "r") as f:
    content = f.read()

content = content.replace(
    'suspend fun markAsRead(@Path("id") id: String, @Body body: Any = Any()): Response<com.strangerhelp.app.data.model.GenericResponse>',
    'suspend fun markAsRead(@Path("id") id: String, @Body body: Any): Response<com.strangerhelp.app.data.model.GenericResponse>'
)

content = content.replace(
    'suspend fun markAllAsRead(@Body body: Any = Any()): Response<com.strangerhelp.app.data.model.GenericResponse>',
    'suspend fun markAllAsRead(@Body body: Any): Response<com.strangerhelp.app.data.model.GenericResponse>'
)

with open(file_path_api, "w") as f:
    f.write(content)

file_path_repo = "app/src/main/java/com/strangerhelp/app/data/repository/NotificationRepository.kt"
with open(file_path_repo, "r") as f:
    content_repo = f.read()

content_repo = content_repo.replace(
    'return api.markAsRead(id)',
    'return api.markAsRead(id, emptyMap<String, Any>())'
)

content_repo = content_repo.replace(
    'return api.markAllAsRead()',
    'return api.markAllAsRead(emptyMap<String, Any>())'
)

with open(file_path_repo, "w") as f:
    f.write(content_repo)
