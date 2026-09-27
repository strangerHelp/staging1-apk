package com.strangerhelp.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.data.local.dao.SearchHistoryDao
import com.strangerhelp.app.data.model.SearchHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(private val searchHistoryDao: SearchHistoryDao) : ViewModel() {

    val recentSearches: StateFlow<List<SearchHistory>> = searchHistoryDao.getRecentSearches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

class TasksViewModelFactory(private val dao: SearchHistoryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
