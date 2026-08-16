package com.strangerhelp.app.data.api

import com.strangerhelp.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface StrangerHelpApi {
    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Map<String, Any>>

    // Tasks
    @GET("api/tasks")
    suspend fun getTasks(
        @Query("category") category: String? = null,
        @Query("mine") mine: String? = null,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int? = null,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("urgent") urgent: String? = null,
        @Query("maxDistance") maxDistance: Int? = null,
    ): Response<List<Task>>

    @GET("api/tasks/{id}")
    suspend fun getTask(@Path("id") id: String, @Query("invite") inviteCode: String? = null): Response<Task>

    @Multipart
    @POST("api/tasks")
    suspend fun postTask(
        @Part("title") title: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody,
        @Part("category") category: okhttp3.RequestBody,
        @Part("budget") budget: okhttp3.RequestBody,
        @Part("location") location: okhttp3.RequestBody,
        @Part("urgent") urgent: okhttp3.RequestBody? = null,
        @Part("visibility") visibility: okhttp3.RequestBody? = null,
        @Part files: List<okhttp3.MultipartBody.Part>? = null
    ): Response<Map<String, String>>

    @PATCH("api/tasks/{id}")
    suspend fun claimTask(@Path("id") id: String, @Body body: Map<String, String>): Response<Map<String, Any>>
    @PATCH("api/tasks/{id}")
    suspend fun updateTracking(@Path("id") id: String, @Body body: Map<String, Any>): Response<Map<String, Any>>

    
    @Multipart
    @PATCH("api/tasks/{id}")
    suspend fun completeTask(@Path("id") id: String, @Part("action") action: okhttp3.RequestBody, @Part proof: okhttp3.MultipartBody.Part?): Response<Map<String, Any>>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Map<String, Any>>

    // Messages
    @POST("api/messages")
    suspend fun createConversation(@Body body: Map<String, String>): Response<Conversation>

    @GET("api/messages")
    suspend fun getConversations(): Response<List<Conversation>>

    @GET("api/messages/{id}")
    suspend fun getMessages(@Path("id") conversationId: String): Response<List<Message>>

    @POST("api/messages/{id}")
    suspend fun sendMessage(@Path("id") conversationId: String, @Body body: Map<String, String>): Response<Message>
    @Multipart
    @POST("api/messages/{id}")
    suspend fun sendMessageMultipart(@Path("id") conversationId: String, @Part text: okhttp3.RequestBody?, @Part files: okhttp3.MultipartBody.Part): Response<Message>


    // Questions
    @GET("api/questions")
    suspend fun getQuestions(@Query("category") category: String? = null): Response<List<Question>>

    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(): Response<NotificationResponse>

    // Meets
    @GET("api/meets")
    suspend fun getMeets(): Response<List<Meet>>

    @POST("api/meets/{id}")
    suspend fun actionMeet(@Path("id") id: String, @Body body: Map<String, String>): Response<Map<String, Any>>

    // Users
    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") id: String): Response<Map<String, Any>>

    // Support & Reports
    @POST("api/support")
    suspend fun contactSupport(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("api/reports")
    suspend fun reportItem(@Body body: Map<String, String>): Response<Map<String, Any>>

    // Pulse
    @GET("api/pulse")
    suspend fun getPulse(): Response<Map<String, Any>>
}
