package com.strangerhelp.app.ui.screens.path

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.model.Path
import com.strangerhelp.app.data.model.PathTask
import com.strangerhelp.app.data.model.PlaceResult
import com.strangerhelp.app.data.repository.PathRepository
import com.strangerhelp.app.data.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PathViewModel(
    private val pathRepository: PathRepository = PathRepository(ApiClient.api)
) : ViewModel() {

    private val _path = MutableStateFlow<Path?>(null)
    val path: StateFlow<Path?> = _path.asStateFlow()

    private val _tasks = MutableStateFlow<List<PathTask>>(emptyList())
    val tasks: StateFlow<List<PathTask>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _matchedTasksCount = MutableStateFlow(0)
    val matchedTasksCount: StateFlow<Int> = _matchedTasksCount.asStateFlow()

    private val _isPathActive = MutableStateFlow(false)
    val isPathActive: StateFlow<Boolean> = _isPathActive.asStateFlow()

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
                    _isPathActive.value = data?.path != null
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to load path"
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
        recurring: Boolean,
        onSuccess: (matchedTasks: Int) -> Unit
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
                    val data = response.body()
                    _matchedTasksCount.value = data?.matchedTasks ?: 0
                    loadPath() // Refresh to get tasks
                    onSuccess(_matchedTasksCount.value)
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

    fun deactivatePath() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = pathRepository.deactivatePath()
                if (response.isSuccessful) {
                    _path.value = null
                    _tasks.value = emptyList()
                    _isPathActive.value = false
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to deactivate path"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun searchPlaces(query: String): List<PlaceResult> {
        if (query.length < 3) return emptyList()
        return try {
            pathRepository.searchPlaces(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getExpiryText(path: Path?): String {
        if (path?.expiresAt == null) return ""
        val expiryDate = parseUTC(path.expiresAt)
        val now = Date()
        val diffMs = expiryDate.time - now.time
        val diffHours = diffMs / (1000 * 60 * 60)
        return if (diffHours > 0) {
            "Expires in ${diffHours.toInt()}h ${((diffMs % (1000*60*60)) / (1000*60)).toInt()}m"
        } else {
            "Expires soon"
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

    private fun parseUTC(timestamp: String): Date {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(timestamp) ?: Date()
        } catch (e: Exception) {
            try {
                val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                sdf2.timeZone = TimeZone.getTimeZone("UTC")
                sdf2.parse(timestamp) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }
    }
}

class PathViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return PathViewModel() as T
    }
}
