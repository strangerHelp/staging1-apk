package com.strangerhelp.app.ui.screens.path

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.strangerhelp.app.data.model.PathTask
import com.strangerhelp.app.util.MapHelper
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

@Composable
fun RouteMapView(
    fromLat: Double,
    fromLng: Double,
    toLat: Double,
    toLng: Double,
    tasks: List<PathTask>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloadProgress by MapHelper.offlineDownloadProgress.collectAsState()

    LaunchedEffect(fromLat, fromLng, toLat, toLng) {
        val midLat = (fromLat + toLat) / 2
        val midLng = (fromLng + toLng) / 2
        MapHelper.downloadOfflineRegion(
            context = context,
            center = LatLng(midLat, midLng),
            radiusKm = 15.0, // Cache a decent radius around the midpoint for offline navigation
            regionName = "RouteOfflineCache"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                MapHelper.initMap(ctx)
                MapView(ctx).apply {
                    getMapAsync { map ->
                        map.setStyle(MapHelper.DEFAULT_STYLE) {
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
            modifier = Modifier.fillMaxSize()
        )

        // Offline caching progress indicator overlay
        if (downloadProgress != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Caching map offline...",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress!! / 100f },
                        modifier = Modifier.width(100.dp).height(4.dp),
                        color = Color(0xFF00BCD4),
                        trackColor = Color.DarkGray
                    )
                }
            }
        }
    }
}
