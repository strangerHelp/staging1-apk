with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    content = f.read()

content = content.replace('''    @POST("api/auth/verify-email")
    suspend fun verifyEmail(): Response<com.strangerhelp.app.data.model.GenericResponse>''', '''    @POST("api/auth/verify-email")
    suspend fun resendVerification(): Response<com.strangerhelp.app.data.model.GenericResponse>
    
    @GET("api/auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Response<com.strangerhelp.app.data.model.GenericResponse>''')

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
    f.write(content)
