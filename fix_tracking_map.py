import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "r") as f:
    text = f.read()

text = text.replace("import com.google.android.gms.maps.CameraUpdateFactory", "")
text = text.replace("import com.google.android.gms.maps.MapView", "import org.maplibre.android.maps.MapView\nimport org.maplibre.android.geometry.LatLng\nimport org.maplibre.android.plugins.annotation.SymbolManager\nimport org.maplibre.android.plugins.annotation.SymbolOptions\nimport org.maplibre.android.camera.CameraPosition")
text = text.replace("import com.google.android.gms.maps.model.CameraPosition", "")
text = text.replace("import com.google.android.gms.maps.model.LatLng", "")
text = text.replace("import com.google.android.gms.maps.model.MarkerOptions", "")

new_map = """@Composable
fun HelperLocationMap(
    helperLat: Double,
    helperLng: Double,
    taskLat: Double,
    taskLng: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { map ->
        map.getMapAsync { maplibreMap ->
            maplibreMap.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                val helperPos = LatLng(helperLat, helperLng)
                val taskPos = LatLng(taskLat, taskLng)
                val camera = CameraPosition.Builder()
                    .target(helperPos)
                    .zoom(14.0)
                    .build()
                maplibreMap.cameraPosition = camera
                
                val symbolManager = SymbolManager(map, maplibreMap, style)
                symbolManager.create(SymbolOptions().withLatLng(helperPos).withTextField("📍 Helper"))
                symbolManager.create(SymbolOptions().withLatLng(taskPos).withTextField("🎯 Task Location"))
            }
        }
    }
}"""

old_map = text[text.find("@Composable\nfun HelperLocationMap("):]

text = text.replace(old_map, new_map)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "w") as f:
    f.write(text)
