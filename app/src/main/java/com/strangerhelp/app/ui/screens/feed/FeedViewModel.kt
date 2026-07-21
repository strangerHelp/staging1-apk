package com.strangerhelp.app.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.StrangerHelpApp
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.utils.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    fun loadTasks(lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch(StrangerHelpApp.globalExceptionHandler) {
            _isLoading.value = true
            try {
                val res = ApiClient.api.getTasks(lat = lat, lng = lng)
                if (res.isSuccessful) {
                    _tasks.value = res.body() ?: emptyList()
                } else {
                    AppLogger.w("FeedViewModel", "Failed to load tasks: ${res.code()}")
                }
            } catch (e: Exception) {
                AppLogger.e("FeedViewModel", "Error loading tasks", e)
            }
            _isLoading.value = false
        }
    }

    fun skipTask() {
        _tasks.value = _tasks.value.drop(1)
    }

    fun claimTask(task: Task) {
        viewModelScope.launch(StrangerHelpApp.globalExceptionHandler) {
            try {
                ApiClient.api.claimTask(task._id, mapOf("action" to "claim"))
            } catch (e: Exception) {
                AppLogger.e("FeedViewModel", "Error claiming task ${task._id}", e)
            }
            _tasks.value = _tasks.value.drop(1)
        }
    }
}
