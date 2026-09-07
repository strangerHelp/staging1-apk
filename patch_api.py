import re

file_path = "app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt"
with open(file_path, "r") as f:
    content = f.read()

new_endpoints = """
    // ⭐ Questions
    @GET("api/questions")
    suspend fun getQuestions(
        @QueryMap queries: Map<String, String>
    ): Response<List<com.strangerhelp.app.data.model.Question>>

    @GET("api/questions/{id}")
    suspend fun getQuestion(
        @Path("id") questionId: String
    ): Response<com.strangerhelp.app.data.model.Question>

    @POST("api/questions")
    suspend fun postQuestion(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<com.google.gson.JsonObject>

    @POST("api/questions/{id}")
    suspend fun postAnswer(
        @Path("id") questionId: String,
        @Body body: Map<String, String>
    ): Response<com.google.gson.JsonObject>

    @POST("api/questions/{id}")
    suspend fun voteAnswer(
        @Path("id") questionId: String,
        @Body body: Map<String, String>
    ): Response<com.google.gson.JsonObject>

    @DELETE("api/questions/{id}")
    suspend fun deleteQuestion(
        @Path("id") questionId: String
    ): Response<com.google.gson.JsonObject>
"""

# Insert before the last closing brace
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + new_endpoints + "\n}"

with open(file_path, "w") as f:
    f.write(content)
