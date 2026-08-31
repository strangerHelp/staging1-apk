import re

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    content = f.read()

injected = """
    @GET("api/meets/{id}")
    suspend fun getMeet(@Path("id") id: String): Response<com.strangerhelp.app.data.model.Meet>

    @GET("api/meets")
    suspend fun getMeetByCode(@Query("code") code: String): Response<com.strangerhelp.app.data.model.Meet>

    @Multipart
    @POST("api/meets")
    suspend fun createMeet(
        @Part("title") title: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody?,
        @Part("category") category: okhttp3.RequestBody,
        @Part("location") location: okhttp3.RequestBody?,
        @Part("date") date: okhttp3.RequestBody,
        @Part("time") time: okhttp3.RequestBody,
        @Part("visibility") visibility: okhttp3.RequestBody,
        @Part("max_attendees") maxAttendees: okhttp3.RequestBody,
        @Part("anonymous") anonymous: okhttp3.RequestBody,
        @Part voiceNote: okhttp3.MultipartBody.Part?
    ): Response<com.strangerhelp.app.data.model.Meet>

    @POST("api/meets/{id}/action")
    suspend fun performMeetAction(@Path("id") id: String, @Body body: com.strangerhelp.app.data.model.MeetActionRequest): Response<com.strangerhelp.app.data.model.MeetActionResponse>

    @DELETE("api/meets/{id}")
    suspend fun deleteMeet(@Path("id") id: String): Response<com.strangerhelp.app.data.model.GenericResponse>
"""

# Insert before last }
last_brace_index = content.rfind("}")
if last_brace_index != -1:
    new_content = content[:last_brace_index] + injected + "\n" + content[last_brace_index:]
    with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
        f.write(new_content)
