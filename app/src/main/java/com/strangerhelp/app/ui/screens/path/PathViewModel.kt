package com.strangerhelp.app.ui.screens.path

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Path
import com.strangerhelp.app.data.model.PathTask
import com.strangerhelp.app.data.repository.PathRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PathUiState(
    val isPathActive: Boolean = false,
    val fromLocation: String = "",
    val toLocation: String = "",
    val radiusKm: Double = 2.0,
    val recurring: Boolean = false
)

class PathViewModel(
    private val pathRepository: PathRepository = PathRepository(ApiClient.api)
) : ViewModel() {

    private val _uiState = MutableStateFlow(PathUiState())
    val uiState: StateFlow<PathUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _tasks = MutableStateFlow<List<PathTask>>(emptyList())
    val tasks: StateFlow<List<PathTask>> = _tasks.asStateFlow()

    private val _path = MutableStateFlow<Path?>(null)
    val path: StateFlow<Path?> = _path.asStateFlow()

    private var refreshJob: Job? = null

    fun loadPath() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = pathRepository.getPath()
                if (response.isSuccessful) {
                    val data = response.body()
                    _path.value = data?.path
                    _tasks.value = data?.tasks ?: emptyList()
                    _uiState.value = _uiState.value.copy(isPathActive = data?.path != null)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                // If it fails with 404 or something, path doesn't exist
                _path.value = null
                _tasks.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setPath(
        fromLocation: String,
        fromLat: Double,
        fromLng: Double,
        toLocation: String,
        toLat: Double,
        toLng: Double,
        radiusKm: Double,
        recurring: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = pathRepository.setPath(
                    fromLocation, fromLat, fromLng,
                    toLocation, toLat, toLng,
                    radiusKm, recurring
                )
                if (response.isSuccessful) {
                    loadPath()
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to set path"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearPath() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = pathRepository.clearPath()
                if (response.isSuccessful) {
                    _path.value = null
                    _tasks.value = emptyList()
                    _uiState.value = _uiState.value.copy(isPathActive = false)
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to clear path"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                loadPath()
                delay(30000) // Refresh every 30 seconds
            }
        }
    }

    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
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

    fun getCurrentLocation(callback: (Double, Double) -> Unit) {
        callback(12.9716, 77.5946)
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}
