import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "r") as f:
    text = f.read()

text = text.replace("import org.maplibre.android.plugins.annotation.SymbolManager", "import org.maplibre.android.annotations.MarkerOptions")
text = text.replace("import org.maplibre.android.plugins.annotation.SymbolOptions", "import com.strangerhelp.app.util.MapHelper")

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
        factory = { 
            MapHelper.initMap(context)
            mapView 
        },
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
                
                maplibreMap.addMarker(MarkerOptions().position(helperPos).title("📍 Helper"))
                maplibreMap.addMarker(MarkerOptions().position(taskPos).title("🎯 Task Location"))
            }
        }
    }
}"""

old_map = text[text.find("@Composable\nfun HelperLocationMap("):]

text = text.replace(old_map, new_map)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TrackingComponents.kt", "w") as f:
    f.write(text)
