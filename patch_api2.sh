sed -i '/@GET("api\/messages")/i \
    @POST("api/messages")\n    suspend fun createConversation(@Body body: Map<String, String>): Response<Conversation>\n' app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt

sed -i '/suspend fun sendMessage/a \
    @Multipart\n    @POST("api/messages/{id}")\n    suspend fun sendMessageMultipart(@Path("id") conversationId: String, @Part text: okhttp3.RequestBody?, @Part files: okhttp3.MultipartBody.Part): Response<Message>\n' app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt

sed -i '/suspend fun claimTask/a \
    @PATCH("api/tasks/{id}")\n    suspend fun updateTracking(@Path("id") id: String, @Body body: Map<String, Any>): Response<Map<String, Any>>\n' app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt
