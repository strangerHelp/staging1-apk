package com.strangerhelp.app.data.repository

import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.UserResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

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

    suspend fun updateProfile(
        name: String? = null,
        handle: String? = null,
        bio: String? = null,
        city: String? = null,
        area: String? = null,
        country: String? = null,
        phone: String? = null,
        avatarFile: File? = null
    ): Response<com.strangerhelp.app.data.model.GenericResponse> {
        val nameBody = name?.toRequestBody("text/plain".toMediaTypeOrNull())
        val handleBody = handle?.toRequestBody("text/plain".toMediaTypeOrNull())
        val bioBody = bio?.toRequestBody("text/plain".toMediaTypeOrNull())
        val cityBody = city?.toRequestBody("text/plain".toMediaTypeOrNull())
        val areaBody = area?.toRequestBody("text/plain".toMediaTypeOrNull())
        val countryBody = country?.toRequestBody("text/plain".toMediaTypeOrNull())
        val phoneBody = phone?.toRequestBody("text/plain".toMediaTypeOrNull())
        val avatarPart = avatarFile?.let {
            val reqBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("avatar", it.name, reqBody)
        }
        return api.updateProfile(
            name = nameBody,
            handle = handleBody,
            bio = bioBody,
            city = cityBody,
            area = areaBody,
            country = countryBody,
            phone = phoneBody,
            avatar = avatarPart
        )
    }
}

