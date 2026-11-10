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

    private val _restrictionInfo = MutableStateFlow(
        com.strangerhelp.app.utils.BackgroundRestrictionHelper.checkRestrictions(context)
    )
    val restrictionInfo: StateFlow<com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionInfo> = _restrictionInfo.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    init {
        refreshRestrictions()
    }

    fun refreshRestrictions() {
        _restrictionInfo.value = com.strangerhelp.app.utils.BackgroundRestrictionHelper.checkRestrictions(context)
    }

    /** Called when the helper taps "Go Online". Returns false if location permissions or background restrictions block it. */
    fun goOnline(taskId: String): Boolean {
        refreshRestrictions()
        val started = WorkerTrackingService.start(context, taskId)
        if (started) {
            _isOnline.value = true
            _currentStatus.value = if (taskId.isNotEmpty()) WorkerStatus.TRACKING else WorkerStatus.ONLINE
            _lastError.value = null
            return true
        } else {
            val restrictions = _restrictionInfo.value
            _lastError.value = restrictions.restrictionSummary
                ?: "Unable to start location service. Please verify permissions and battery settings."
            return false
        }
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

    fun handleRestrictionFix(context: Context) {
        when (_restrictionInfo.value.recommendedAction) {
            com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionAction.OPEN_BATTERY_OPTIMIZATION_SETTINGS -> {
                com.strangerhelp.app.utils.BackgroundRestrictionHelper.openBatteryOptimizationSettings(context)
            }
            com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionAction.OPEN_BATTERY_SAVER_SETTINGS -> {
                com.strangerhelp.app.utils.BackgroundRestrictionHelper.openBatterySaverSettings(context)
            }
            com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionAction.OPEN_DATA_SAVER_SETTINGS -> {
                com.strangerhelp.app.utils.BackgroundRestrictionHelper.openDataSaverSettings(context)
            }
            else -> {
                com.strangerhelp.app.utils.BackgroundRestrictionHelper.openAppDetailsSettings(context)
            }
        }
    }
}

class WorkerStatusViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkerStatusViewModel(application) as T
    }
}
