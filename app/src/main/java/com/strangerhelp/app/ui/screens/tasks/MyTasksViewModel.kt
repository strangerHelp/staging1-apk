package com.strangerhelp.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.repository.AuthRepository
import com.strangerhelp.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyTasksViewModel(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository = AuthRepository(ApiClient.api)
) : ViewModel() {

    private var rawTasks: List<Task> = emptyList()
    private var currentUserId: String? = null

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeTab = MutableStateFlow("all")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _postedCount = MutableStateFlow(0)
    val postedCount: StateFlow<Int> = _postedCount.asStateFlow()

    private val _claimedCount = MutableStateFlow(0)
    val claimedCount: StateFlow<Int> = _claimedCount.asStateFlow()

    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount.asStateFlow()

    fun setFilter(filter: String) {
        _activeTab.value = filter.lowercase()
        applyFilter()
    }

    fun loadTasks(filter: String = "all", userId: String? = null) {
        _activeTab.value = filter.lowercase()
        if (!userId.isNullOrBlank()) {
            currentUserId = userId
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (currentUserId.isNullOrBlank()) {
                    try {
                        val authRes = authRepository.getCurrentUser()
                        if (authRes.isSuccessful) {
                            currentUserId = authRes.body()?.user?.id
                        }
                    } catch (_: Exception) {}
                }

                val response = taskRepository.getMyTasks(filter = "all")
                if (response.isSuccessful) {
                    rawTasks = response.body() ?: emptyList()
                    updateCounts()
                    applyFilter()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateCounts() {
        val uid = currentUserId
        _postedCount.value = rawTasks.count { isPostedTask(it, uid) }
        _claimedCount.value = rawTasks.count { isClaimedTask(it, uid) }
        _completedCount.value = rawTasks.count { isCompletedTask(it) }
    }

    fun isPostedTask(task: Task, uid: String?): Boolean {
        return if (!uid.isNullOrBlank()) {
            task.posterId == uid
        } else {
            task.posterId.isNotBlank()
        }
    }

    fun isClaimedTask(task: Task, uid: String?): Boolean {
        return if (!uid.isNullOrBlank()) {
            val isClaimer = task.claimedBy == uid || task.claimedUsers?.any { it.userId == uid } == true
            // Strictly exclude tasks posted by this user
            task.posterId != uid && isClaimer
        } else {
            task.status.equals("claimed", ignoreCase = true)
        }
    }

    fun isCompletedTask(task: Task): Boolean {
        return task.status.equals("completed", ignoreCase = true) ||
               task.completionStatus.equals("approved", ignoreCase = true)
    }

    private fun applyFilter() {
        val uid = currentUserId
        val filtered = when (_activeTab.value) {
            "posted" -> rawTasks.filter { isPostedTask(it, uid) }
            "claimed" -> rawTasks.filter { isClaimedTask(it, uid) }
            "completed" -> rawTasks.filter { isCompletedTask(it) }
            else -> rawTasks
        }
        _tasks.value = filtered
    }
}

class MyTasksViewModelFactory(
    private val repository: TaskRepository,
    private val authRepository: AuthRepository = AuthRepository(ApiClient.api)
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyTasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyTasksViewModel(repository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
