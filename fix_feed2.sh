cat << 'INNER_EOF' > app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedViewModel.kt
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
import kotlinx.coroutines.flow.first

class FeedViewModel : ViewModel() {
    private val db = StrangerHelpApp.instance.database
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Load initial from DB
            _tasks.value = db.taskDao().getAllTasks().first()
        }
    }

    fun loadTasks(lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch(StrangerHelpApp.globalExceptionHandler) {
            _isLoading.value = true
            try {
                val res = ApiClient.api.getTasks(lat = lat, lng = lng)
                if (res.isSuccessful) {
                    val body = res.body() ?: emptyList()
                    db.taskDao().insertTasks(body)
                    _tasks.value = body
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
        val current = _tasks.value.firstOrNull()
        _tasks.value = _tasks.value.drop(1)
        if (current != null) {
            viewModelScope.launch { db.taskDao().deleteTaskById(current._id) }
        }
    }

    fun claimTask(task: Task) {
        viewModelScope.launch(StrangerHelpApp.globalExceptionHandler) {
            try {
                ApiClient.api.claimTask(task._id, mapOf("action" to "claim"))
            } catch (e: Exception) {
                AppLogger.e("FeedViewModel", "Error claiming task ${task._id}", e)
            }
            _tasks.value = _tasks.value.drop(1)
            db.taskDao().deleteTaskById(task._id)
        }
    }
}
INNER_EOF
