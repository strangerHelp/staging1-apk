import re

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    content = f.read()

# I added these:
# @POST("/api/auth/forgot")
# suspend fun requestPasswordReset(@Body request: Map<String, String>): Response<com.strangerhelp.app.data.model.GenericResponse>
# 
# @POST("/api/auth/reset")
# suspend fun resetPassword(@Body request: Map<String, String>): Response<com.strangerhelp.app.data.model.GenericResponse>
# I will just regex replace them.

content = re.sub(r'@POST\("/api/auth/forgot"\)\s*suspend fun requestPasswordReset[^\n]*\n', "", content)
content = re.sub(r'@POST\("/api/auth/reset"\)\s*suspend fun resetPassword[^\n]*\n', "", content)

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
    f.write(content)

