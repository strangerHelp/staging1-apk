import re

file_path = "app/src/main/java/com/strangerhelp/app/service/TrackingService.kt"
with open(file_path, "r") as f:
    content = f.read()

import_monitor = "import com.strangerhelp.app.utils.BatteryMonitor\n"
if "import com.strangerhelp.app.utils.BatteryMonitor" not in content:
    content = content.replace("import com.strangerhelp.app.R\n", "import com.strangerhelp.app.R\n" + import_monitor)

on_start = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        taskId = intent?.getStringExtra("taskId") ?: run {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(
            TRACKING_NOTIFICATION_ID,
            buildNotification("Sharing your location for a task")
        )

        scope.launch(Dispatchers.Main) {
            BatteryMonitor.isBatterySaverMode.collect { isBatterySaver ->
                fusedClient.removeLocationUpdates(locationCallback)
                
                val interval = if (isBatterySaver) 60000L else 15000L
                val minInterval = if (isBatterySaver) 30000L else 10000L
                val minDistance = if (isBatterySaver) 100f else 50f
                
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    interval
                )
                    .setMinUpdateIntervalMillis(minInterval)
                    .setMinUpdateDistanceMeters(minDistance)
                    .build()

                try {
                    fusedClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } catch (e: SecurityException) {
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }"""

old_on_start = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
    }"""

content = content.replace(old_on_start, on_start)

with open(file_path, "w") as f:
    f.write(content)
