package com.strangerhelp.app.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Diagnostic and execution helper to detect and handle background task restrictions
 * across older and modern Android versions (Android 8.0 Oreo through Android 14+).
 *
 * Prevents crashes caused by:
 * - IllegalStateException: Not allowed to start service ...: app is in background (API 26+)
 * - ForegroundServiceStartNotAllowedException (API 31+)
 * - RemoteServiceException: Context.startForegroundService() did not then call Service.startForeground() (API 26-31)
 * - ActivityManager.isBackgroundRestricted() battery bucket throttling (API 28+)
 * - PowerManager Doze mode & battery saver network cutoffs (API 23+)
 * - ConnectivityManager Data Saver restriction
 */
object BackgroundRestrictionHelper {

    private const val TAG = "BackgroundRestrictionHelper"

    enum class BackgroundRestrictionAction {
        NONE,
        OPEN_BATTERY_OPTIMIZATION_SETTINGS,
        OPEN_BATTERY_SAVER_SETTINGS,
        OPEN_DATA_SAVER_SETTINGS,
        OPEN_APP_SETTINGS
    }

    data class BackgroundRestrictionInfo(
        val isBackgroundRestricted: Boolean,
        val isPowerSaveMode: Boolean,
        val isDeviceIdleMode: Boolean,
        val isDataSaverActive: Boolean,
        val isBatteryOptimized: Boolean,
        val hasRestrictions: Boolean,
        val restrictionSummary: String?,
        val recommendedAction: BackgroundRestrictionAction
    )

    sealed class ServiceStartResult {
        object Success : ServiceStartResult()
        data class Restricted(val reason: String) : ServiceStartResult()
        data class PermissionDenied(val missingPermission: String) : ServiceStartResult()
        data class Failed(val error: Throwable) : ServiceStartResult()
    }

    /**
     * Checks all background execution constraints for the device and application.
     */
    fun checkRestrictions(context: Context): BackgroundRestrictionInfo {
        val isBgRestricted = isBackgroundRestricted(context)
        val isPowerSave = isPowerSaveMode(context)
        val isIdle = isDeviceIdleMode(context)
        val isDataSaver = isDataSaverActive(context)
        val isBatOptimized = !isIgnoringBatteryOptimizations(context)

        val warnings = mutableListOf<String>()
        var action = BackgroundRestrictionAction.NONE

        if (isBgRestricted) {
            warnings.add("Background restriction is enabled in battery settings. Location updates may stop when the app is minimized.")
            action = BackgroundRestrictionAction.OPEN_APP_SETTINGS
        } else if (isPowerSave) {
            warnings.add("Battery Saver is active. Background updates may be delayed or paused by the OS.")
            action = BackgroundRestrictionAction.OPEN_BATTERY_SAVER_SETTINGS
        } else if (isDataSaver) {
            warnings.add("Data Saver is active. Background network sync is limited to Wi-Fi.")
            action = BackgroundRestrictionAction.OPEN_DATA_SAVER_SETTINGS
        } else if (isBatOptimized) {
            // Informational only, not necessarily blocking
            action = BackgroundRestrictionAction.OPEN_BATTERY_OPTIMIZATION_SETTINGS
        }

        val hasRestrictions = isBgRestricted || isPowerSave || isDataSaver

        return BackgroundRestrictionInfo(
            isBackgroundRestricted = isBgRestricted,
            isPowerSaveMode = isPowerSave,
            isDeviceIdleMode = isIdle,
            isDataSaverActive = isDataSaver,
            isBatteryOptimized = isBatOptimized,
            hasRestrictions = hasRestrictions,
            restrictionSummary = warnings.firstOrNull(),
            recommendedAction = action
        )
    }

    /**
     * Checks if the user or OEM has placed the app in background restriction (API 28+).
     */
    fun isBackgroundRestricted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            return activityManager?.isBackgroundRestricted == true
        }
        return false
    }

    /**
     * Checks if the device is currently in Power Save (Battery Saver) mode.
     */
    fun isPowerSaveMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isPowerSaveMode == true
    }

    /**
     * Checks if the device is currently in Doze / Device Idle mode (API 23+).
     */
    fun isDeviceIdleMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isDeviceIdleMode == true
    }

    /**
     * Checks if the app is whitelisted from battery optimizations (Doze mode exemption).
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    /**
     * Checks if Data Saver is blocking background network access.
     */
    fun isDataSaverActive(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm?.isActiveNetworkMetered == true) {
                return cm.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
            }
        }
        return false
    }

    /**
     * Safely starts a foreground service, shielding the calling thread and app from:
     * - IllegalStateException (starting foreground service while app is in background on API 26+)
     * - ForegroundServiceStartNotAllowedException (API 31+)
     * - SecurityException (missing permissions or foreground service restrictions)
     */
    fun safeStartForegroundService(context: Context, intent: Intent): ServiceStartResult {
        // Pre-check background restrictions on Android 9+
        if (isBackgroundRestricted(context)) {
            AppLogger.w(TAG, "Cannot start foreground service: app is under OS background restriction.")
            return ServiceStartResult.Restricted(
                "App is background-restricted by system battery policy."
            )
        }

        return try {
            ContextCompat.startForegroundService(context, intent)
            ServiceStartResult.Success
        } catch (e: SecurityException) {
            AppLogger.w(TAG, "SecurityException starting foreground service: ${e.message}", e)
            ServiceStartResult.PermissionDenied(e.message ?: "Permission denied for foreground service")
        } catch (e: IllegalStateException) {
            // Covers ForegroundServiceStartNotAllowedException (Android 12+) and background start restrictions (Android 8+)
            AppLogger.w(TAG, "IllegalStateException starting foreground service: ${e.message}", e)
            ServiceStartResult.Restricted(e.message ?: "Service start not allowed by background restrictions")
        } catch (e: Throwable) {
            AppLogger.e(TAG, "Unexpected error starting foreground service: ${e.message}", e)
            ServiceStartResult.Failed(e)
        }
    }

    /**
     * Opens system battery optimization settings for the user to whitelist the app.
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to open battery optimization settings: ${e.message}")
            openAppDetailsSettings(context)
        }
    }

    /**
     * Opens system battery saver settings.
     */
    fun openBatterySaverSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to open battery saver settings: ${e.message}")
            openAppDetailsSettings(context)
        }
    }

    /**
     * Opens system data saver settings.
     */
    fun openDataSaverSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val intent = Intent("android.settings.DATA_SAVER_SETTINGS").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                openAppDetailsSettings(context)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to open data saver settings: ${e.message}")
            openAppDetailsSettings(context)
        }
    }

    /**
     * Opens the application detail settings screen (where users can adjust Permissions, Battery, and Data).
     */
    fun openAppDetailsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to open app details settings: ${e.message}")
        }
    }
}
