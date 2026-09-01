import re

file_path = "app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(
    'suspend fun markAsRead(@Path("id") id: String): Response<com.strangerhelp.app.data.model.GenericResponse>',
    'suspend fun markAsRead(@Path("id") id: String, @Body body: Any = Any()): Response<com.strangerhelp.app.data.model.GenericResponse>'
)

content = content.replace(
    'suspend fun markAllAsRead(): Response<com.strangerhelp.app.data.model.GenericResponse>',
    'suspend fun markAllAsRead(@Body body: Any = Any()): Response<com.strangerhelp.app.data.model.GenericResponse>'
)

with open(file_path, "w") as f:
    f.write(content)
