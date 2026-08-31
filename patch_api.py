import re
with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    content = f.read()

replacement = """    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(): Response<NotificationResponse>

    @PATCH("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<com.strangerhelp.app.data.model.GenericResponse>

    @PATCH("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<com.strangerhelp.app.data.model.GenericResponse>
"""

content = re.sub(
    r'    // Notifications\n    @GET\("api/notifications"\)\n    suspend fun getNotifications\(\): Response<NotificationResponse>',
    replacement,
    content
)

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
    f.write(content)
