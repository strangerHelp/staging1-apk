package com.strangerhelp.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _isSendingVerification = MutableStateFlow(false)
    val isSendingVerification: StateFlow<Boolean> = _isSendingVerification.asStateFlow()

    private val _verificationMessage = MutableStateFlow<String?>(null)
    val verificationMessage: StateFlow<String?> = _verificationMessage.asStateFlow()

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> = _verificationError.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _verificationSuccess = MutableStateFlow(false)
    val verificationSuccess: StateFlow<Boolean> = _verificationSuccess.asStateFlow()

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _isSendingVerification.value = true
            _verificationMessage.value = null
            _verificationError.value = null

            try {
                val response = ApiClient.api.resendVerification()
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data?.message?.contains("already verified") == true) {
                        _verificationMessage.value = "Your email is already verified!"
                    } else {
                        _verificationMessage.value = "✅ Verification email sent! Please check your inbox."
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _verificationError.value = parseError(errorBody)
                }
            } catch (e: Exception) {
                _verificationError.value = "Failed to send verification email. Please try again."
            } finally {
                _isSendingVerification.value = false
            }
        }
    }

    fun verifyEmail(token: String) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationError.value = null
            _verificationSuccess.value = false

            try {
                val response = ApiClient.api.verifyEmail(token)
                if (response.isSuccessful) {
                    _verificationSuccess.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    _verificationError.value = parseError(errorBody) ?: "Invalid or expired link"
                }
            } catch (e: Exception) {
                _verificationError.value = "Network error. Please check your connection."
            } finally {
                _isVerifying.value = false
            }
        }
    }

    fun clearVerificationState() {
        _verificationMessage.value = null
        _verificationError.value = null
        _verificationSuccess.value = false
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
}
