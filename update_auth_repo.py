with open("app/src/main/java/com/strangerhelp/app/data/repository/AuthRepository.kt", "r") as f:
    content = f.read()

replacement = """    suspend fun getCurrentUser(): Response<UserResponse> {
        return api.getMe()
    }
    
    suspend fun resendVerification(): Response<com.strangerhelp.app.data.model.GenericResponse> {
        return api.resendVerification()
    }

    suspend fun verifyEmail(token: String): Response<com.strangerhelp.app.data.model.GenericResponse> {
        return api.verifyEmail(token)
    }"""

content = content.replace("    suspend fun getCurrentUser(): Response<UserResponse> {\n        return api.getMe()\n    }", replacement)

with open("app/src/main/java/com/strangerhelp/app/data/repository/AuthRepository.kt", "w") as f:
    f.write(content)
