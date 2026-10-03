package com.strangerhelp.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Notification
import com.strangerhelp.app.data.repository.NotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepository(ApiClient.api)
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var pollJob: Job? = null

    init {
        startPolling()
    }

    // ⭐ Load notifications
    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = notificationRepository.getNotifications()
                if (response.isSuccessful) {
                    val data = response.body()
                    _notifications.value = data?.notifications ?: emptyList()
                    _unreadCount.value = data?.unreadCount ?: 0
                } else {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to load notifications"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ⭐ Mark notification as read
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val response = notificationRepository.markAsRead(notificationId)
                if (response.isSuccessful) {
                    // Update local state directly to be responsive
                    val updatedList = _notifications.value.map { 
                        if (it.id == notificationId) it.copy(read = 1) else it 
                    }
                    _notifications.value = updatedList
                    _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Error marking all as read", e)
            }
        }
    }

    // ⭐ Mark all as read
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val response = notificationRepository.markAllAsRead()
                if (response.isSuccessful) {
                    loadNotifications()
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Error marking all as read", e)
            }
        }
    }

    // ⭐ Inject test notification for verification
    fun addTestNotification(
        title: String = "Task Claimed! (Test)",
        message: String = "A helper has claimed your task. Tap to view details and start chatting.",
        type: String = "task_claimed",
        link: String = "/tasks"
    ) {
        val testItem = Notification(
            id = "test_${System.currentTimeMillis()}",
            user_id = "test_user",
            type = type,
            title = title,
            message = message,
            link = link,
            read = 0,
            created_at = "Just now"
        )
        _notifications.value = listOf(testItem) + _notifications.value
        _unreadCount.value = _unreadCount.value + 1
    }

    // ⭐ Start polling
    fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                loadNotifications()
                delay(30000) // Poll every 30 seconds
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
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

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

class NotificationViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
