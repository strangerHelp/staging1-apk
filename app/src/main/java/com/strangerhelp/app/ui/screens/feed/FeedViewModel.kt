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

    init {
        viewModelScope.launch {
            try {
                val cached = db.taskDao().getAllTasksList()
                if (cached.isNotEmpty()) {
                    _recentTasks.value = cached.take(5)
                } else {
                    _recentTasks.value = com.strangerhelp.app.data.repository.DEFAULT_SEED_TASKS.take(5)
                }
            } catch (_: Exception) {
                _recentTasks.value = com.strangerhelp.app.data.repository.DEFAULT_SEED_TASKS.take(5)
            }
        }
    }

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

                // 2. Load Recent Tasks with Room cache
                try {
                    val tasksRes = ApiClient.api.getTasks(limit = 10)
                    if (tasksRes.isSuccessful) {
                        val list = tasksRes.body() ?: emptyList()
                        if (list.isNotEmpty()) {
                            _recentTasks.value = list.take(5)
                            db.taskDao().insertTasks(list)
                        } else {
                            val cached = db.taskDao().getAllTasksList()
                            _recentTasks.value = if (cached.isNotEmpty()) cached.take(5) else com.strangerhelp.app.data.repository.DEFAULT_SEED_TASKS.take(5)
                        }
                    } else {
                        val cached = db.taskDao().getAllTasksList()
                        _recentTasks.value = if (cached.isNotEmpty()) cached.take(5) else com.strangerhelp.app.data.repository.DEFAULT_SEED_TASKS.take(5)
                    }
                } catch (e: Exception) {
                    val cached = db.taskDao().getAllTasksList()
                    _recentTasks.value = if (cached.isNotEmpty()) cached.take(5) else com.strangerhelp.app.data.repository.DEFAULT_SEED_TASKS.take(5)
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.e("FeedViewModel", "Error loading home data", e)
                try {
                    val cached = db.taskDao().getAllTasksList()
                    if (cached.isNotEmpty()) {
                        _recentTasks.value = cached.take(5)
                    }
                } catch (_: Exception) {}
            }
            _isLoading.value = false
        }
    }
}
