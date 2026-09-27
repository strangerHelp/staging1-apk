package com.strangerhelp.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.strangerhelp.app.MainActivity
import com.strangerhelp.app.R
import com.strangerhelp.app.data.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that shares the helper's live location with the task
 * poster while a task is in progress.
 *
 * The service is ONLY started after the user explicitly taps "Start Task
 * (Share Live Location)" on the Task Detail screen. It displays a persistent
 * notification so the user is always aware that location is being shared.
 */
class TrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private var taskId: String = ""
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                sendLocationUpdate(loc.latitude, loc.longitude)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()  // MUST run before startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle the "Stop sharing" action from the notification
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            return START_NOT_STICKY
        }

        taskId = intent?.getStringExtra(EXTRA_TASK_ID) ?: run {
            Log.e(TAG, "Missing taskId extra; stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification()

        // Android 10+ requires the FGS type to be declared explicitly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
        if (taskId.isNotEmpty()) {
            scope.launch {
                runCatching {
                    ApiClient.api.updateTracking(
                        taskId,
                        mapOf<String, Any>("action" to "stop_tracking")
                    )
                }
            }
        }
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---------------------------------------------------------------------

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L  // 10 seconds
        )
            .setMinUpdateIntervalMillis(5_000L)  // 5 seconds min
            .build()

        try {
            // Get immediate last location first
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    sendLocationUpdate(it.latitude, it.longitude)
                }
            }
            
            fusedClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission revoked mid-tracking", e)
            stopSelf()
        }
    }

    private fun sendLocationUpdate(lat: Double, lng: Double) {
        if (taskId.isEmpty()) return
        scope.launch {
            runCatching {
                ApiClient.api.updateTracking(
                    taskId,
                    mapOf<String, Any>(
                        "action" to "update_location",
                        "lat" to lat,
                        "lng" to lng
                    )
                )
            }.onFailure { Log.e(TAG, "Failed to push location", it) }
        }
    }

    private fun stopTracking() {
        fusedClient.removeLocationUpdates(locationCallback)
        if (taskId.isNotEmpty()) {
            scope.launch {
                runCatching {
                    ApiClient.api.updateTracking(
                        taskId,
                        mapOf<String, Any>("action" to "stop_tracking")
                    )
                }
            }
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Sharing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows your location while helping with a task"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            this, 0, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, StopTrackingReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getBroadcast(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("📍 Sharing your location")
            .setContentText("The task poster can see your position while you complete this task.")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(openPending)
            .addAction(
                R.drawable.ic_stop,
                "Stop sharing",
                stopPending
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val TAG = "TrackingService"
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1001
        private const val EXTRA_TASK_ID = "taskId"

        const val ACTION_STOP = "com.strangerhelp.app.STOP_TRACKING"

        /** Start the foreground tracking service for the given task. */
        fun start(context: Context, taskId: String) {
            val intent = Intent(context, TrackingService::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Stop the foreground tracking service. */
        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingService::class.java))
        }
    }
}
