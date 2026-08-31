package com.strangerhelp.app.ui.screens.tasks

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.repository.TaskRepository
import com.strangerhelp.app.utils.GpsCameraHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GpsCameraViewModel(
    private val taskRepository: TaskRepository,
    private val locationHelper: GpsCameraHelper
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val _placeName = MutableStateFlow("")
    val placeName: StateFlow<String> = _placeName.asStateFlow()

    private val _isLocationReady = MutableStateFlow(false)
    val isLocationReady: StateFlow<Boolean> = _isLocationReady.asStateFlow()

    private val _isCheckingLocation = MutableStateFlow(false)
    val isCheckingLocation: StateFlow<Boolean> = _isCheckingLocation.asStateFlow()

    init {
        checkLocation()
    }

    fun checkLocation() {
        viewModelScope.launch {
            _isCheckingLocation.value = true

            if (!locationHelper.hasLocationPermission()) {
                _error.value = "Location permission required"
                _isCheckingLocation.value = false
                return@launch
            }

            val loc = locationHelper.requireLocation()
            if (loc != null) {
                _location.value = loc
                _placeName.value = locationHelper.getPlaceName(loc.latitude, loc.longitude)
                _isLocationReady.value = true
            } else {
                _error.value = "Unable to get location. Please enable GPS and try again."
            }

            _isCheckingLocation.value = false
        }
    }

    fun retryLocation() {
        checkLocation()
    }

    fun submitProof(
        taskId: String,
        proofBytes: ByteArray,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            try {
                val response = taskRepository.submitProofBytes(taskId, proofBytes)

                if (response.isSuccessful) {
                    _success.value = true
                    onResult(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = parseError(errorBody)
                    onResult(false)
                }
            } catch (e: Exception) {
                _error.value = "Failed to submit proof. Please try again."
                onResult(false)
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

class GpsCameraViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = com.strangerhelp.app.data.api.ApiClient.api
        val repository = TaskRepository(api)
        val locationHelper = GpsCameraHelper(context)
        if (modelClass.isAssignableFrom(GpsCameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GpsCameraViewModel(repository, locationHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
