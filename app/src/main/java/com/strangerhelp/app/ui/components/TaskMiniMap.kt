package com.strangerhelp.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

@Composable
fun TaskMiniMap(lat: Double, lng: Double, location: String) {
    Column {
        // MapView showing pin
        AndroidView(
            factory = { context ->
                com.strangerhelp.app.util.MapHelper.initMap(context)
                MapView(context).apply {
                    getMapAsync { map ->
                        map.setStyle("https://tiles.openfreemap.org/styles/liberty") { _ ->
                            map.cameraPosition = CameraPosition.Builder()
                                .target(LatLng(lat, lng))
                                .zoom(14.0)
                                .build()
                            map.addMarker(MarkerOptions().position(LatLng(lat, lng)).title(location))
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        
        // "Get Directions" button - opens Google Maps
        val context = LocalContext.current
        OutlinedButton(
            onClick = {
                val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Icon(Icons.Filled.Directions, null)
            Spacer(Modifier.width(8.dp))
            Text("Get Directions (Google Maps)")
        }
    }
}
