package com.strangerhelp.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class VerificationViewModel : ViewModel() {

    private val _status = MutableStateFlow<VerificationStatus?>(null)
    val status: StateFlow<VerificationStatus?> = _status.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    fun loadStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.api.getVerificationStatus()
                if (response.isSuccessful) {
                    _status.value = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    fun submitVerification(
        idType: String,
        idNumber: String,
        frontFile: File,
        selfieFile: File,
        backFile: File?
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            _success.value = false

            try {
                val idTypeBody = idType.toRequestBody("text/plain".toMediaTypeOrNull())
                val idNumberBody = idNumber.toRequestBody("text/plain".toMediaTypeOrNull())
                
                val frontBody = frontFile.asRequestBody("image/*".toMediaTypeOrNull())
                val frontPart = MultipartBody.Part.createFormData("front", frontFile.name, frontBody)
                
                val selfieBody = selfieFile.asRequestBody("image/*".toMediaTypeOrNull())
                val selfiePart = MultipartBody.Part.createFormData("selfie", selfieFile.name, selfieBody)
                
                val backPart = backFile?.let {
                    val backBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("back", it.name, backBody)
                }

                val response = ApiClient.api.submitVerification(
                    idType = idTypeBody,
                    idNumber = idNumberBody,
                    front = frontPart,
                    selfie = selfiePart,
                    back = backPart
                )

                if (response.isSuccessful) {
                    _success.value = true
                    loadStatus()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to submit verification. Please try again."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Something went wrong"
        return try {
            val json = Gson().fromJson(errorBody, JsonObject::class.java)
            json.get("error")?.asString ?: "Something went wrong"
        } catch (_: Exception) {
            "Something went wrong"
        }
    }

    fun clearError() {
        _error.value = null
    }
}
