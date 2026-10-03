package com.strangerhelp.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.local.dao.SearchHistoryDao
import com.strangerhelp.app.data.model.SearchHistory
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.TaskPriority
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

    private val _selectedCity = MutableStateFlow("Mumbai")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _userLat = MutableStateFlow<Double?>(null)
    val userLat: StateFlow<Double?> = _userLat.asStateFlow()

    private val _userLng = MutableStateFlow<Double?>(null)
    val userLng: StateFlow<Double?> = _userLng.asStateFlow()

    private val _selectedPriority = MutableStateFlow<TaskPriority?>(null)
    val selectedPriority: StateFlow<TaskPriority?> = _selectedPriority.asStateFlow()

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

    fun setLocation(cityName: String, lat: Double? = null, lng: Double? = null) {
        _selectedCity.value = cityName
        _userLat.value = lat
        _userLng.value = lng
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _selectedPriority.value = priority
    }

    fun fetchTasks(
        category: String? = null,
        query: String? = null,
        sortBy: String = "newest",
        locationName: String? = null,
        priority: TaskPriority? = _selectedPriority.value,
        lat: Double? = null,
        lng: Double? = null,
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

        val effectiveCity = locationName ?: _selectedCity.value
        val effectiveLat = lat ?: _userLat.value
        val effectiveLng = lng ?: _userLng.value
        val effectivePriority = priority ?: _selectedPriority.value

        viewModelScope.launch {
            try {
                val cleanCat = if (category == "All") null else category
                val result = taskRepository.getTasks(
                    category = cleanCat,
                    limit = pageSize,
                    offset = currentOffset,
                    search = query,
                    sort = sortBy,
                    lat = effectiveLat,
                    lng = effectiveLng
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

                val processed = enrichAndFilterByLocation(rawList, effectiveCity, effectiveLat, effectiveLng)
                val priorityFiltered = if (effectivePriority != null) {
                    processed.filter { it.getEffectivePriority() == effectivePriority }
                } else {
                    processed
                }
                _tasks.value = applySort(priorityFiltered, sortBy)
                if (result.tasks.isNotEmpty()) {
                    currentOffset += pageSize
                }
            } catch (e: Exception) {
                // Network error: load from Room local database cache
                _isOffline.value = true
                _isFromCache.value = true
                val cleanCat = if (category == "All") null else category
                val cached = taskRepository.getCachedTasks(cleanCat, query)
                val processed = enrichAndFilterByLocation(cached, effectiveCity, effectiveLat, effectiveLng)
                val priorityFiltered = if (effectivePriority != null) {
                    processed.filter { it.getEffectivePriority() == effectivePriority }
                } else {
                    processed
                }
                _tasks.value = applySort(priorityFiltered, sortBy)
                _hasMore.value = false
                _errorMessage.value = "Offline mode: viewing locally cached tasks"
            } finally {
                _isLoading.value = false
                _isLoadingMore.value = false
            }
        }
    }

    private fun enrichAndFilterByLocation(
        list: List<Task>,
        locationName: String?,
        lat: Double?,
        lng: Double?
    ): List<Task> {
        val effectiveLoc = locationName?.trim()
        val isAll = effectiveLoc.isNullOrBlank() ||
                effectiveLoc.equals("All", ignoreCase = true) ||
                effectiveLoc.equals("All Locations", ignoreCase = true)

        // Calculate distance if user coordinates provided
        val enriched = if (lat != null && lng != null) {
            list.map { task ->
                if (task.lat != null && task.lng != null) {
                    val dist = haversine(lat, lng, task.lat, task.lng)
                    task.copy(distance = dist)
                } else {
                    task
                }
            }
        } else {
            list
        }

        if (isAll) return enriched

        val locQuery = effectiveLoc ?: ""
        val matched = enriched.filter { task ->
            task.city.contains(locQuery, ignoreCase = true) ||
            task.location.contains(locQuery, ignoreCase = true) ||
            locQuery.contains(task.city, ignoreCase = true) ||
            (task.distance != null && task.distance <= 50.0) // Within 50km radius
        }

        // If tasks exist matching location, return them; otherwise return all with distance enriched
        return if (matched.isNotEmpty()) matched else enriched
    }

    private fun applySort(list: List<Task>, sortBy: String): List<Task> {
        return when (sortBy) {
            "budget_high" -> list.sortedByDescending { it.budget }
            "budget_low" -> list.sortedBy { it.budget }
            "urgent", "priority" -> list.sortedWith(
                compareBy<Task> { it.getEffectivePriority().ordinal }
                    .thenByDescending { it.urgent }
                    .thenByDescending { it.createdAt }
            )
            "nearest", "distance" -> list.sortedBy { it.distance ?: Double.MAX_VALUE }
            else -> list // Default newest (already ordered by createdAt in query/api)
        }
    }

    fun buildLocationLabel(): String {
        val lat = _userLat.value
        val lng = _userLng.value
        val city = _selectedCity.value
        return if (lat != null && lng != null) {
            "Tasks near you ($city)"
        } else {
            "Tasks in $city"
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

