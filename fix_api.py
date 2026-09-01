import re

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "r") as f:
    content = f.read()

replacement = """    // Reviews
    @POST("api/reviews")
    suspend fun submitReview(@Body request: com.strangerhelp.app.data.model.SubmitReviewRequest): Response<com.google.gson.JsonObject>

    @GET("api/reviews")
    suspend fun getReviewsByUser(@Query("userId") userId: String): Response<com.strangerhelp.app.data.model.ReviewsResponse>

    @GET("api/reviews")
    suspend fun getReviewsByTask(@Query("taskId") taskId: String): Response<com.strangerhelp.app.data.model.ReviewsResponse>"""

content = re.sub(r'    // Reviews\s*@POST\("api/reviews"\)\s*suspend fun postReview[^\)]*\): Response<com\.strangerhelp\.app\.data\.model\.GenericResponse>', replacement, content)

with open("app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt", "w") as f:
    f.write(content)
