package com.strangerhelp.app.data.repository

import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.UserResponse
import retrofit2.Response

class AuthRepository(private val api: StrangerHelpApi) {
    suspend fun getCurrentUser(): Response<UserResponse> {
        return api.getMe()
    }
    
    suspend fun resendVerification(): Response<com.strangerhelp.app.data.model.GenericResponse> {
        return api.resendVerification()
    }

    suspend fun verifyEmail(token: String): Response<com.strangerhelp.app.data.model.GenericResponse> {
        return api.verifyEmail(token)
    }
}
