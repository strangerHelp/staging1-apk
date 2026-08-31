import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/AuthViewModel.kt", "r") as f:
    content = f.read()

new_functions = """
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun requestReset(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = ApiClient.api.forgotPassword(mapOf("email" to email.trim().lowercase()))
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = parseError(errorBody)
                    onResult(false)
                }
            } catch (e: Exception) {
                _error.value = "Network error. Please try again."
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(token: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = ApiClient.api.resetPassword(mapOf("token" to token, "password" to newPassword))
                if (response.isSuccessful) {
                    ApiClient.clearCookies()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseError(errorBody).takeIf { it != "Something went wrong" } ?: "Reset failed. Please request a new link."
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                onError("Network error. Please try again.")
            } finally {
                _isLoading.value = false
            }
        }
    }
"""

content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + new_functions + "\n}"

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/AuthViewModel.kt", "w") as f:
    f.write(content)

