package com.strangerhelp.app.ui.screens.path

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.strangerhelp.app.data.model.PathTask
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import com.strangerhelp.app.util.MapHelper

@Composable
fun RouteMapView(
    fromLat: Double,
    fromLng: Double,
    toLat: Double,
    toLng: Double,
    tasks: List<PathTask>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            MapHelper.initMap(context)
            MapView(context).apply {
                getMapAsync { map ->
                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") {
                        // Center on midpoint
                        val midLat = (fromLat + toLat) / 2
                        val midLng = (fromLng + toLng) / 2
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(midLat, midLng))
                            .zoom(11.0)
                            .build()

                        // Add markers for tasks within radius
                        tasks.forEach { task ->
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(task.lat, task.lng))
                                    .title(task.title)
                                    .snippet("₹${task.budget}")
                            )
                        }

                        // Add from/to markers
                        map.addMarker(
                            MarkerOptions()
                                .position(LatLng(fromLat, fromLng))
                                .title("Start")
                                .snippet("Your starting point")
                        )
                        map.addMarker(
                            MarkerOptions()
                                .position(LatLng(toLat, toLng))
                                .title("Destination")
                                .snippet("Your ending point")
                        )
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}
