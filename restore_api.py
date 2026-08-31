import re

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    content = f.read()

# The injected block:
injected = """
    // Meets
    @GET("api/meets")
    suspend fun getMeets(): Response<List<com.strangerhelp.app.data.model.Meet>>

    @GET("api/meets/{id}")
    suspend fun getMeet(@Path("id") id: String): Response<com.strangerhelp.app.data.model.Meet>

    @GET("api/meets")
    suspend fun getMeetByCode(@Query("code") code: String): Response<com.strangerhelp.app.data.model.Meet>

    @Multipart
    @POST("api/meets")
    suspend fun createMeet(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("category") category: RequestBody,
        @Part("location") location: RequestBody?,
        @Part("date") date: RequestBody,
        @Part("time") time: RequestBody,
        @Part("visibility") visibility: RequestBody,
        @Part("max_attendees") maxAttendees: RequestBody,
        @Part("anonymous") anonymous: RequestBody,
        @Part voiceNote: MultipartBody.Part?
    ): Response<com.strangerhelp.app.data.model.Meet>

    @POST("api/meets/{id}")
    suspend fun performMeetAction(@Path("id") id: String, @Body body: com.strangerhelp.app.data.model.MeetActionRequest): Response<com.strangerhelp.app.data.model.MeetActionResponse>

    @DELETE("api/meets/{id}")
    suspend fun deleteMeet(@Path("id") id: String): Response<com.strangerhelp.app.data.model.GenericResponse>
"""

# Let's just remove the injected block entirely.
# Because the injected block contains newlines, replace it carefully.
clean_content = content.replace(injected + "\n", "")

# Now append it ONCE at the end of the file, right before the last closing brace.
if clean_content.endswith("}"):
    clean_content = clean_content[:-1] + injected + "\n}"

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
    f.write(clean_content)

print("Restored")
