import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

target = """            MapView(
                lat = task.helperLat!!,
                lng = task.helperLng!!,
                marker = "Helper",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )"""

replacement = """            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    org.maplibre.android.maps.MapView(ctx).apply {
                        setTag("poster_map_${System.currentTimeMillis()}")
                        getMapAsync { map ->
                            try {
                                map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                                    val cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                        .target(org.maplibre.android.geometry.LatLng(task.helperLat!!, task.helperLng!!))
                                        .zoom(14.0)
                                        .build()
                                    map.cameraPosition = cameraPosition
                                    val markerOptions = org.maplibre.android.annotations.MarkerOptions()
                                        .position(org.maplibre.android.geometry.LatLng(task.helperLat!!, task.helperLng!!))
                                        .title("Helper")
                                    map.addMarker(markerOptions)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)
