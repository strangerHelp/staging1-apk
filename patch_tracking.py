import re

with open("app/src/main/java/com/strangerhelp/app/service/TrackingService.kt", "r") as f:
    content = f.read()

# Replace startLocationUpdates
new_start_updates = """
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
"""

old_start_updates_pattern = r"private fun startLocationUpdates\(\).*?catch \(e: SecurityException\) \{.*?\}\s*\}"
content = re.sub(old_start_updates_pattern, new_start_updates.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/service/TrackingService.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "r") as f:
    content = f.read()

# Add loadTask(taskId) to startTracking
content = content.replace("_isTracking.value = true\n        }", "_isTracking.value = true\n            loadTask(taskId)\n        }")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt", "w") as f:
    f.write(content)
