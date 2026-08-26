import re

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    content = f.read()

new_endpoints = """    @GET("api/auth/verify")
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
"""

if "getVerificationStatus" not in content:
    content = content.replace("    // Tasks", new_endpoints + "\n    // Tasks")

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
    f.write(content)
