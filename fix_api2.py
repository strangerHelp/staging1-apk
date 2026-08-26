import re

with open('app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt', 'r') as f:
    content = f.read()

# Fix the broken {id ... } replacements
bad_pattern = r'\{id.*?suspend fun postReview\(@Body body: Map<String, Any>\): Response<com\.strangerhelp\.app\.data\.model\.GenericResponse>\}'
content = re.sub(bad_pattern, '{id}', content, flags=re.DOTALL)

# Now add postReview at the end properly
if "fun postReview" not in content:
    content = content.replace("}", """
    // Reviews
    @POST("api/reviews")
    suspend fun postReview(@Body body: Map<String, Any>): Response<com.strangerhelp.app.data.model.GenericResponse>
}""")

with open('app/src/main/java/com/strangerhelp/app/data/api/StrangerHelpApi.kt', 'w') as f:
    f.write(content)
