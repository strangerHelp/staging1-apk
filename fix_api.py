import re

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    content = f.read()

new_endpoints = """
    @POST("/api/auth/forgot")
    suspend fun requestPasswordReset(@Body request: Map<String, String>): Response<com.strangerhelp.app.data.model.GenericResponse>

    @POST("/api/auth/reset")
    suspend fun resetPassword(@Body request: Map<String, String>): Response<com.strangerhelp.app.data.model.GenericResponse>
"""

# add it before the closing brace
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + new_endpoints + "\n}"

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
    f.write(content)
