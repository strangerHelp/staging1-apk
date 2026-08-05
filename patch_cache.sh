sed -i 's/import kotlin.random.Random/import kotlin.random.Random\nimport org.maplibre.android.offline.OfflineManager/' app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt

cat << 'INNER' > replace.txt
    // Ensure MapLibre is initialized
    LaunchedEffect(Unit) {
        try {
            MapLibre.getInstance(context)
            val offlineManager = OfflineManager.getInstance(context)
            offlineManager.setMaximumAmbientCacheSize(150 * 1024 * 1024, object : OfflineManager.FileSourceCallback {
                override fun onSuccess() {}
                override fun onError(message: String) {}
            })
        } catch (e: Exception) {}
    }
INNER

sed -i '/\/\/ Ensure MapLibre is initialized/,/    }/c\    \/\/ Ensure MapLibre is initialized\n    LaunchedEffect(Unit) {\n        try {\n            MapLibre.getInstance(context)\n            val offlineManager = OfflineManager.getInstance(context)\n            offlineManager.setMaximumAmbientCacheSize(150 * 1024 * 1024, object : OfflineManager.FileSourceCallback {\n                override fun onSuccess() {}\n                override fun onError(message: String) {}\n            })\n        } catch (e: Exception) {}\n    }' app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt
