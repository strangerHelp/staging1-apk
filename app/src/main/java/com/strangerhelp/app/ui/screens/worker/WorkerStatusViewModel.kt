package com.strangerhelp.app.ui.screens.worker

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.strangerhelp.app.StrangerHelpApp
import com.strangerhelp.app.service.WorkerStatus
import com.strangerhelp.app.service.WorkerTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkerStatusViewModel(
    application: Application = StrangerHelpApp.instance
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _currentStatus = MutableStateFlow(WorkerStatus.OFFLINE)
    val currentStatus: StateFlow<WorkerStatus> = _currentStatus.asStateFlow()

    /** Called when the helper taps "Go Online". Returns false if location permissions are missing. */
    fun goOnline(taskId: String): Boolean {
        val started = WorkerTrackingService.start(context, taskId)
        if (started) {
            _isOnline.value = true
            _currentStatus.value = if (taskId.isNotEmpty()) WorkerStatus.TRACKING else WorkerStatus.ONLINE
            return true
        }
        return false
    }

    /** Called when the helper taps "Go Offline" or when the task completes. */
    fun goOffline() {
        WorkerTrackingService.stop(context)
        _isOnline.value = false
        _currentStatus.value = WorkerStatus.OFFLINE
    }

    /** Called from task lifecycle events to update the persistent banner. */
    fun updateStatus(status: WorkerStatus) {
        WorkerTrackingService.updateStatus(context, status)
        _currentStatus.value = status
    }
}

class WorkerStatusViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkerStatusViewModel(application) as T
    }
}
