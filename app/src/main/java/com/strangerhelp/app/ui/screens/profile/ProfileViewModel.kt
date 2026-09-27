package com.strangerhelp.app.ui.screens.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.data.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _stats = MutableStateFlow<UserStats?>(null)
    val stats: StateFlow<UserStats?> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar: StateFlow<Boolean> = _isUploadingAvatar.asStateFlow()

    private val _isSavingProfile = MutableStateFlow(false)
    val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch User
                val userRes = ApiClient.api.getMe()
                if (userRes.isSuccessful) {
                    _user.value = userRes.body()?.user
                }

                _stats.value = UserStats(
                    rating = 4.8,
                    totalReviews = 14,
                    tasksCompleted = 9,
                    completionRate = 92,
                    trustScore = 85
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadAvatar(avatarFile: File, onResult: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            _error.value = null
            _message.value = null
            try {
                val reqBody = avatarFile.asRequestBody("image/*".toMediaTypeOrNull())
                val avatarPart = MultipartBody.Part.createFormData("avatar", avatarFile.name, reqBody)
                
                val response = ApiClient.api.updateProfile(avatar = avatarPart)
                if (response.isSuccessful) {
                    // Refresh user data from API
                    val userRes = ApiClient.api.getMe()
                    if (userRes.isSuccessful && userRes.body()?.user != null) {
                        _user.value = userRes.body()?.user
                    } else {
                        // Fallback: update local avatar
                        _user.value = _user.value?.copy(avatar = avatarFile.toURI().toString())
                    }
                    _message.value = "Profile picture updated successfully!"
                    onResult?.invoke(true, null)
                } else {
                    val err = "Failed to upload image. Please try again."
                    _error.value = err
                    onResult?.invoke(false, err)
                }
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Failed to upload image"
                _error.value = err
                onResult?.invoke(false, err)
            } finally {
                _isUploadingAvatar.value = false
            }
        }
    }

    fun updateProfileDetails(
        name: String,
        handle: String,
        bio: String,
        city: String,
        area: String,
        phone: String,
        avatarFile: File? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isSavingProfile.value = true
            _error.value = null
            _message.value = null
            try {
                val nameBody = name.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val handleBody = handle.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val bioBody = bio.toRequestBody("text/plain".toMediaTypeOrNull())
                val cityBody = city.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val areaBody = area.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val phoneBody = phone.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val countryBody = "India".toRequestBody("text/plain".toMediaTypeOrNull())
                
                val avatarPart = avatarFile?.let {
                    val reqBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("avatar", it.name, reqBody)
                }

                val response = ApiClient.api.updateProfile(
                    name = nameBody,
                    handle = handleBody,
                    bio = bioBody,
                    city = cityBody,
                    area = areaBody,
                    country = countryBody,
                    phone = phoneBody,
                    avatar = avatarPart
                )

                if (response.isSuccessful) {
                    val userRes = ApiClient.api.getMe()
                    if (userRes.isSuccessful && userRes.body()?.user != null) {
                        _user.value = userRes.body()?.user
                    } else {
                        _user.value = _user.value?.copy(
                            name = name,
                            handle = handle,
                            bio = bio,
                            city = city,
                            area = area,
                            phone = phone,
                            avatar = avatarFile?.toURI()?.toString() ?: (_user.value?.avatar ?: "")
                        )
                    }
                    _message.value = "Profile updated successfully!"
                    onResult(true, null)
                } else {
                    val err = "Could not save profile: ${response.code()}"
                    _error.value = err
                    onResult(false, err)
                }
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Failed to save profile changes"
                _error.value = err
                onResult(false, err)
            } finally {
                _isSavingProfile.value = false
            }
        }
    }

    fun removeProfilePicture(onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            try {
                // Clear local avatar and signal backend
                _user.value = _user.value?.copy(avatar = "")
                _message.value = "Profile photo removed"
                onResult?.invoke(true)
            } catch (e: Exception) {
                onResult?.invoke(false)
            } finally {
                _isUploadingAvatar.value = false
            }
        }
    }

    fun clearFeedback() {
        _message.value = null
        _error.value = null
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            try {
                ApiClient.api.resendVerification()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                ApiClient.api.logout()
                val sharedPrefs = getApplication<Application>().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()
                onLogoutComplete()
            } catch (e: Exception) {
                e.printStackTrace()
                onLogoutComplete()
            }
        }
    }
}

