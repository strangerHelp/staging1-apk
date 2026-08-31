package com.strangerhelp.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.strangerhelp.app.R
import com.strangerhelp.app.MainActivity
import com.strangerhelp.app.data.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class TrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private var taskId: String = ""
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                scope.launch {
                    try {
                        val response = ApiClient.api.updateTracking(
                            taskId,
                            mapOf(
                                "action" to "update_location",
                                "lat" to location.latitude,
                                "lng" to location.longitude
                            )
                        )
                        if (!response.isSuccessful) {
                            // Log error but don't stop tracking
                        }
                    } catch (e: IOException) {
                        // Network error — will retry on next update
                    } catch (e: HttpException) {
                        // 403 Forbidden — helper is no longer assigned
                        if (e.code() == 403) {
                            stopTracking()
                        }
                    } catch (e: Exception) {
                        
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        taskId = intent?.getStringExtra("taskId") ?: run {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(
            TRACKING_NOTIFICATION_ID,
            buildNotification("Sharing your location for a task")
        )

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            15000L // 15 seconds
        )
            .setMinUpdateIntervalMillis(10000L) // 10 seconds minimum
            .setMinUpdateDistanceMeters(50f) // 50 meters
            .build()

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Location permission revoked mid-tracking
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
        // Attempt to stop tracking on server
        if (taskId.isNotEmpty()) {
            scope.launch {
                try {
                    ApiClient.api.updateTracking(
                        taskId,
                        mapOf("action" to "stop_tracking")
                    )
                } catch (_: Exception) {
                    // Ignore errors on destroy
                }
            }
        }
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopTracking() {
        if (taskId.isNotEmpty()) {
            scope.launch {
                try {
                    ApiClient.api.updateTracking(
                        taskId,
                        mapOf("action" to "stop_tracking")
                    )
                } catch (_: Exception) { }
            }
        }
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows your location while helping with a task"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(message: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("📍 Sharing your location")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Using launcher since ic_location is missing probably
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val TRACKING_NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "tracking_channel"

        fun start(context: Context, taskId: String) {
            val intent = Intent(context, TrackingService::class.java).apply {
                putExtra("taskId", taskId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingService::class.java))
        }
    }
}
