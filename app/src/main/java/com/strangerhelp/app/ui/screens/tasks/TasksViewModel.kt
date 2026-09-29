package com.strangerhelp.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.local.dao.SearchHistoryDao
import com.strangerhelp.app.data.model.SearchHistory
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val searchHistoryDao: SearchHistoryDao,
    private val taskRepository: TaskRepository = TaskRepository(ApiClient.api)
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _isFromCache = MutableStateFlow(false)
    val isFromCache: StateFlow<Boolean> = _isFromCache.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 20

    val recentSearches: StateFlow<List<SearchHistory>> = searchHistoryDao.getRecentSearches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Pre-populate with cached tasks from Room database immediately so user never sees a blank screen
        viewModelScope.launch {
            val initialCached = taskRepository.getCachedTasks()
            if (initialCached.isNotEmpty() && _tasks.value.isEmpty()) {
                _tasks.value = initialCached
            }
        }
    }

    fun fetchTasks(
        category: String? = null,
        query: String? = null,
        sortBy: String = "newest",
        isLoadMore: Boolean = false
    ) {
        if (!isLoadMore) {
            _isLoading.value = true
            currentOffset = 0
            _hasMore.value = true
            _errorMessage.value = null
        } else {
            if (_isLoadingMore.value || !_hasMore.value || _isOffline.value) return
            _isLoadingMore.value = true
        }

        viewModelScope.launch {
            try {
                val cleanCat = if (category == "All") null else category
                val result = taskRepository.getTasks(
                    category = cleanCat,
                    limit = pageSize,
                    offset = currentOffset,
                    search = query
                )

                _isOffline.value = result.isOffline
                _isFromCache.value = result.isFromCache
                _hasMore.value = result.hasMore
                _errorMessage.value = result.errorMessage

                val rawList = if (isLoadMore) {
                    _tasks.value + result.tasks
                } else {
                    if (result.tasks.isEmpty() && result.isOffline) {
                        // Fallback to all cached tasks if filtered query returned empty
                        taskRepository.getCachedTasks(cleanCat, query)
                    } else {
                        result.tasks
                    }
                }

                _tasks.value = applySort(rawList, sortBy)
                if (result.tasks.isNotEmpty()) {
                    currentOffset += pageSize
                }
            } catch (e: Exception) {
                // Network error: load from Room local database cache
                _isOffline.value = true
                _isFromCache.value = true
                val cleanCat = if (category == "All") null else category
                val cached = taskRepository.getCachedTasks(cleanCat, query)
                _tasks.value = applySort(cached, sortBy)
                _hasMore.value = false
                _errorMessage.value = "Offline mode: viewing locally cached tasks"
            } finally {
                _isLoading.value = false
                _isLoadingMore.value = false
            }
        }
    }

    private fun applySort(list: List<Task>, sortBy: String): List<Task> {
        return when (sortBy) {
            "budget_high" -> list.sortedByDescending { it.budget }
            "budget_low" -> list.sortedBy { it.budget }
            "urgent" -> list.sortedByDescending { it.urgent }
            else -> list // Default newest (already ordered by createdAt in query/api)
        }
    }

    fun saveSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            searchHistoryDao.insertSearch(
                SearchHistory(
                    query = trimmed,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteSearch(query: String) {
        viewModelScope.launch {
            searchHistoryDao.deleteSearch(query)
        }
    }

    fun clearAllSearches() {
        viewModelScope.launch {
            searchHistoryDao.clearHistory()
        }
    }
}

class TasksViewModelFactory(
    private val dao: SearchHistoryDao,
    private val repository: TaskRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val repo = repository ?: TaskRepository(ApiClient.api)
            return TasksViewModel(dao, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

