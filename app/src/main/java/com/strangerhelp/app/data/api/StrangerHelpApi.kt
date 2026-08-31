package com.strangerhelp.app.data.api

import com.strangerhelp.app.data.model.AuthResponse
import com.strangerhelp.app.data.model.UserResponse
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.Conversation
import com.strangerhelp.app.data.model.Message
import com.strangerhelp.app.data.model.Question
import com.strangerhelp.app.data.model.NotificationResponse
import com.strangerhelp.app.data.model.Meet
import com.strangerhelp.app.data.model.PathResponse
import retrofit2.Response
import retrofit2.http.*

interface StrangerHelpApi {
    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>
    
    @POST("api/auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>
    
    @POST("api/auth/forgot")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<com.strangerhelp.app.data.model.GenericResponse>
    
    @POST("api/auth/reset")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<com.strangerhelp.app.data.model.GenericResponse>
    
    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponse>
    
    @POST("api/auth/verify-email")
    suspend fun resendVerification(): Response<com.strangerhelp.app.data.model.GenericResponse>
    
    @GET("api/auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Response<com.strangerhelp.app.data.model.GenericResponse>
    
    @POST("api/auth/logout")
    suspend fun logout(): Response<Map<String, Any>>
    
    @GET("api/auth/verify")
    suspend fun getVerificationStatus(): Response<com.strangerhelp.app.data.model.VerificationStatus>
    
    @Multipart
    @POST("api/auth/verify")
    suspend fun submitVerification(
        @Part("idType") idType: okhttp3.RequestBody,
        @Part("idNumber") idNumber: okhttp3.RequestBody,
        @Part front: okhttp3.MultipartBody.Part,
        @Part selfie: okhttp3.MultipartBody.Part,
        @Part back: okhttp3.MultipartBody.Part? = null
    ): Response<com.strangerhelp.app.data.model.GenericResponse>

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
    
    @POST("api/tasks")
    suspend fun postTask(@Body body: okhttp3.RequestBody): Response<Map<String, @JvmSuppressWildcards Any>>
    
    @PATCH("api/tasks/{id}")
    suspend fun claimTask(@Path("id") id: String, @Body body: com.strangerhelp.app.data.model.ClaimTaskRequest): Response<com.strangerhelp.app.data.model.ClaimResponse>
    
    @PATCH("api/tasks/{id}")
    suspend fun updateTracking(@Path("id") id: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, Any>>
        
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
    
    @GET("api/messages/conversation/{id}")
    suspend fun getConversation(@Path("id") conversationId: String): Response<Conversation>
    
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

    @PATCH("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<com.strangerhelp.app.data.model.GenericResponse>

    @PATCH("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<com.strangerhelp.app.data.model.GenericResponse>

    
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

    // Path
    @POST("api/path")
    suspend fun setPath(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<com.strangerhelp.app.data.model.PathSetResponse>

    @GET("api/path")
    suspend fun getPath(): Response<PathResponse>

    @DELETE("api/path")
    suspend fun clearPath(): Response<com.google.gson.JsonObject>

    // Reviews
    @POST("api/reviews")
    suspend fun postReview(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<com.strangerhelp.app.data.model.GenericResponse>

    @GET("api/support")
    suspend fun getSupportMessages(): Response<com.strangerhelp.app.data.model.SupportResponse>

    @POST("api/support")
    suspend fun sendSupportMessage(
        @Body body: Map<String, String>
    ): Response<com.strangerhelp.app.data.model.SupportResponse>

    
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


    
    
}