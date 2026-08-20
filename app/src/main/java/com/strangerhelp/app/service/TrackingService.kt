package com.strangerhelp.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.strangerhelp.app.MainActivity
import com.strangerhelp.app.R
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.utils.BatteryMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class TrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentTaskId: String? = null
    private var isBatteryLow = false
    private var batteryMonitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { loc ->
                    val taskId = currentTaskId ?: return@let
                    serviceScope.launch {
                        try {
                            ApiClient.api.updateTracking(
                                taskId,
                                mapOf(
                                    "action" to "update_location",
                                    "lat" to loc.latitude,
                                    "lng" to loc.longitude
                                )
                            )
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            }
        }
        
        // Monitor battery saver mode and adjust frequency dynamically
        batteryMonitorJob = serviceScope.launch {
            BatteryMonitor.isBatterySaverMode.collect { low ->
                if (isBatteryLow != low) {
                    isBatteryLow = low
                    if (currentTaskId != null) {
                        startLocationUpdates()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskId = intent?.getStringExtra("taskId")
        if (taskId != null) {
            currentTaskId = taskId
            startForegroundService()
            startLocationUpdates()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "tracking_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Live Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("StrangerHelp Tracking")
            .setContentText("Sharing live location with the task poster...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // fallback icon
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        // Stop previous updates if running to switch intervals smoothly
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Adjust tracking intensity based on battery state
        val interval = if (isBatteryLow) 30000L else 10000L
        val minInterval = if (isBatteryLow) 15000L else 5000L
        val priority = if (isBatteryLow) Priority.PRIORITY_BALANCED_POWER_ACCURACY else Priority.PRIORITY_HIGH_ACCURACY

        val request = LocationRequest.Builder(priority, interval)
            .setMinUpdateIntervalMillis(minInterval)
            .build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Missing permissions
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryMonitorJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        // Optionally notify server that tracking stopped if not done via UI
        val taskId = currentTaskId
        if (taskId != null) {
            serviceScope.launch {
                try {
                    ApiClient.api.updateTracking(taskId, mapOf("action" to "stop_tracking"))
                } catch (e: Exception) {}
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null
}
