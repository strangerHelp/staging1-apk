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

data class UserStats(
    val tasksPosted: Int = 0,
    val tasksClaimed: Int = 0,
    val tasksCompleted: Int = 0
)

class FeedViewModel : ViewModel() {
    private val db = StrangerHelpApp.instance.database
    
    private val _recentTasks = MutableStateFlow<List<Task>>(emptyList())
    val recentTasks = _recentTasks.asStateFlow()
    
    private val _stats = MutableStateFlow(UserStats())
    val stats = _stats.asStateFlow()
    
    private val _pulseData = MutableStateFlow(Pair(0, 0)) // helpers, tasks
    val pulseData = _pulseData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    fun loadHomeData(userId: String?) {
        viewModelScope.launch(StrangerHelpApp.globalExceptionHandler) {
            _isLoading.value = true
            try {
                // 1. Load User Stats
                if (userId != null) {
                    val statsRes = ApiClient.api.getUserProfile(userId)
                    if (statsRes.isSuccessful) {
                        val body = statsRes.body()
                        if (body != null) {
                            val posted = (body["tasksPosted"] as? Double)?.toInt() ?: 0
                            val claimed = (body["tasksClaimed"] as? Double)?.toInt() ?: 0
                            val completed = (body["tasksCompleted"] as? Double)?.toInt() ?: 0
                            _stats.value = UserStats(posted, claimed, completed)
                        }
                    }
                }

                // 2. Load Recent Tasks
                val tasksRes = ApiClient.api.getTasks(mine = "true", limit = 5)
                if (tasksRes.isSuccessful) {
                    _recentTasks.value = tasksRes.body() ?: emptyList()
                }

                // 3. Load Pulse
                val pulseRes = ApiClient.api.getPulse()
                if (pulseRes.isSuccessful) {
                    val pulseBody = pulseRes.body()
                    if (pulseBody != null) {
                        val helpers = ((pulseBody["helpers"] as? List<*>)?.size) ?: ((pulseBody["helpers"] as? Double)?.toInt() ?: 0)
                        val tasks = ((pulseBody["tasks"] as? List<*>)?.size) ?: ((pulseBody["tasks"] as? Double)?.toInt() ?: 0)
                        _pulseData.value = Pair(helpers, tasks)
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("FeedViewModel", "Error loading home data", e)
            }
            _isLoading.value = false
        }
    }
}
