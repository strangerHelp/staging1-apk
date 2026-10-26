package com.strangerhelp.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps a persistent Ola / Uber / Porter style
 * notification alive while the helper is online and sharing location.
 */
class WorkerTrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var taskId: String = ""
    private var currentStatus: WorkerStatus = WorkerStatus.ONLINE
    private var elapsedJob: Job? = null
    private var elapsedSeconds: Long = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                pushLocation(loc.latitude, loc.longitude)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle action buttons from the notification
        when (intent?.action) {
            ACTION_STOP -> {
                stopWork()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_STATUS -> {
                val statusName = intent.getStringExtra(EXTRA_STATUS)
                val newStatus = runCatching {
                    WorkerStatus.valueOf(statusName ?: "")
                }.getOrNull() ?: currentStatus
                updateStatus(newStatus)
                return START_STICKY
            }
        }

        taskId = intent?.getStringExtra(EXTRA_TASK_ID).orEmpty()
        currentStatus = if (taskId.isNotEmpty()) WorkerStatus.TRACKING else WorkerStatus.ONLINE

        // Promote to foreground immediately (must be within 5s)
        val hasPerm = hasLocationPermission(this)
        if (!hasPerm) {
            Log.w(TAG, "Location permission missing; cannot start location foreground service.")
            stopSelf()
            return START_NOT_STICKY
        }

        val initial = buildNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    initial,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, initial)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException during startForeground", e)
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start foreground service", e)
            stopSelf()
            return START_NOT_STICKY
        }

        startTimer()
        startLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing location updates", e)
        }
        elapsedJob?.cancel()
        if (taskId.isNotEmpty()) {
            scope.launch {
                runCatching {
                    ApiClient.api.patchTask(
                        taskId,
                        mapOf("action" to "stop_tracking")
                    )
                }
            }
        }
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ========================================================================
    // NOTIFICATION
    // ========================================================================

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Worker Status",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows your online status and active task"
                enableLights(true)
                lightColor = Color.parseColor("#F5A623")
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        // Tap to open the app
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Stop Work" action
        val stopIntent = Intent(this, WorkerTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "View Task" action (deep link into task detail)
        val viewTaskIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse(
                if (taskId.isNotEmpty()) "https://strangerhelp.com/tasks/$taskId"
                else "https://strangerhelp.com/tasks"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val viewTaskPending = PendingIntent.getActivity(
            this, 2, viewTaskIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = (elapsedSeconds / 60).toString().padStart(2, '0')
        val seconds = (elapsedSeconds % 60).toString().padStart(2, '0')

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🟢 ${currentStatus.title}")
            .setContentText("${currentStatus.text}  ·  $minutes:$seconds")
            .setSmallIcon(R.drawable.ic_notification_worker)
            .setColor(Color.parseColor("#F5A623"))
            .setOngoing(true)                     // ← persistent, cannot swipe
            .setOnlyAlertOnce(true)               // ← no sound on every update
            .setShowWhen(false)
            .setContentIntent(openPending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Action buttons
        if (currentStatus.showStop) {
            builder.addAction(
                R.drawable.ic_stop,
                "Stop Work",
                stopPending
            )
        }
        if (taskId.isNotEmpty()) {
            builder.addAction(
                R.drawable.ic_task,
                "View Task",
                viewTaskPending
            )
        }

        // Expanded view for a richer, Uber-style layout
        val expandedText = buildString {
            append(currentStatus.text)
            append("\n")
            append("● Time online: $minutes:$seconds")
            if (taskId.isNotEmpty()) {
                append("\n● Task ID: ${taskId.take(8)}…")
            }
        }
        builder.setStyle(
            NotificationCompat.BigTextStyle().bigText(expandedText)
        )

        return builder.build()
    }

    private fun refreshNotification() {
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager?.notify(NOTIFICATION_ID, buildNotification())
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing notification", e)
        }
    }

    // ========================================================================
    // TIMER
    // ========================================================================

    private fun startTimer() {
        elapsedJob?.cancel()
        elapsedSeconds = 0
        elapsedJob = scope.launch {
            while (isActive) {
                delay(1_000)
                elapsedSeconds++
                // Update the notification every 10 seconds (avoid spam)
                if (elapsedSeconds % 10 == 0L) {
                    refreshNotification()
                }
            }
        }
    }

    // ========================================================================
    // LOCATION
    // ========================================================================

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            15_000L
        )
            .setMinUpdateIntervalMillis(10_000L)
            .setMinUpdateDistanceMeters(50f)
            .build()

        try {
            fusedClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting location updates", e)
        }
    }

    private fun pushLocation(lat: Double, lng: Double) {
        if (taskId.isEmpty()) return
        scope.launch {
            runCatching {
                ApiClient.api.patchTask(
                    taskId,
                    mapOf(
                        "action" to "update_location",
                        "lat" to lat,
                        "lng" to lng
                    )
                )
            }.onFailure { Log.e(TAG, "push location failed", it) }
        }
    }

    // ========================================================================
    // STATUS + STOP
    // ========================================================================

    fun updateStatus(status: WorkerStatus) {
        currentStatus = status
        refreshNotification()
    }

    private fun stopWork() {
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing location updates", e)
        }
        elapsedJob?.cancel()
        if (taskId.isNotEmpty()) {
            scope.launch {
                runCatching {
                    ApiClient.api.patchTask(
                        taskId,
                        mapOf("action" to "stop_tracking")
                    )
                }
            }
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    companion object {
        private const val TAG = "WorkerTrackingService"
        const val CHANNEL_ID = "worker_tracking_channel"
        const val NOTIFICATION_ID = 101
        const val EXTRA_TASK_ID = "taskId"
        const val EXTRA_STATUS = "status"

        const val ACTION_STOP = "com.strangerhelp.app.ACTION_STOP"
        const val ACTION_UPDATE_STATUS = "com.strangerhelp.app.ACTION_UPDATE_STATUS"

        fun hasLocationPermission(context: Context): Boolean {
            val fine = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            return fine || coarse
        }

        /** Start the worker service for a given task. */
        fun start(context: Context, taskId: String): Boolean {
            if (!hasLocationPermission(context)) {
                Log.w(TAG, "Cannot start WorkerTrackingService: location permission is not granted.")
                return false
            }
            val intent = Intent(context, WorkerTrackingService::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
            }
            return try {
                ContextCompat.startForegroundService(context, intent)
                true
            } catch (e: SecurityException) {
                Log.w(TAG, "SecurityException starting WorkerTrackingService", e)
                false
            } catch (e: Exception) {
                Log.w(TAG, "Failed to startForegroundService", e)
                false
            }
        }

        /** Update the notification status without restarting the service. */
        fun updateStatus(context: Context, status: WorkerStatus) {
            val intent = Intent(context, WorkerTrackingService::class.java).apply {
                action = ACTION_UPDATE_STATUS
                putExtra(EXTRA_STATUS, status.name)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to startForegroundService for updateStatus", e)
            }
        }

        /** Stop the service and remove the notification. */
        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, WorkerTrackingService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stopService", e)
            }
        }
    }
}
