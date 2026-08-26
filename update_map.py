import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.annotations.MarkerOptions
"""

if "org.maplibre.android" not in content:
    content = content.replace("import java.util.*", "import java.util.*\n" + imports)


old_map_section = """
@Composable
fun MapSection(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, HairlineColor)
    ) {
        Column {
            // Fake map view for now
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp).background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(48.dp), tint = MutedText)
                Text("Map Preview", modifier = Modifier.padding(top = 70.dp), color = MutedText, fontWeight = FontWeight.Medium)
            }
            Text("📍 ${task.location}", fontSize = 13.sp, color = Color(0xFF4D4D4D), modifier = Modifier.padding(16.dp))
            
            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    if (task.lat != null && task.lng != null) {
                        openGoogleMapsDirections(context, task.lat, task.lng)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 8.dp),
                border = BorderStroke(1.dp, AccentOrange),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange)
            ) {
                Icon(Icons.Outlined.Navigation, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Get Directions (Google Maps)")
            }
        }
    }
}
"""

new_map_section = """
@Composable
fun MapSection(task: Task) {
    val lat = task.lat
    val lng = task.lng
    val location = task.location
    val context = LocalContext.current

    if (lat == null || lng == null) {
        // No location available
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceVariantColor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MutedText
                    )
                    Text(
                        text = "No location provided",
                        fontSize = 14.sp,
                        color = MutedText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, HairlineColor)
    ) {
        Column {
            // Map Preview
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTag("task_map_${System.currentTimeMillis()}")
                        getMapAsync { map ->
                            try {
                                // Set style (free, no API key)
                                map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                                    // Position the camera
                                    val cameraPosition = CameraPosition.Builder()
                                        .target(LatLng(lat, lng))
                                        .zoom(14.0)
                                        .build()
                                    map.cameraPosition = cameraPosition

                                    // Add a marker
                                    val markerOptions = MarkerOptions()
                                        .position(LatLng(lat, lng))
                                        .title(location ?: "Task Location")
                                        .snippet("Tap for directions")
                                    map.addMarker(markerOptions)
                                }
                            } catch (e: Exception) {
                                // Handle map load errors
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // Location Address and Directions Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Location address
                Text(
                    text = "📍 ${location ?: "Unknown location"}",
                    fontSize = 12.sp,
                    color = PrimaryDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Get Directions Button
                OutlinedButton(
                    onClick = {
                        openGoogleMapsDirections(context, lat, lng, location)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentOrange
                    ),
                    border = BorderStroke(1.dp, AccentOrange)
                ) {
                    Icon(
                        Icons.Outlined.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccentOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get Directions (Google Maps)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
"""

content = content.replace(old_map_section.strip(), new_map_section.strip())

# Need to update openGoogleMapsDirections to accept destination string
old_open_google_maps = """
fun openGoogleMapsDirections(context: Context, lat: Double, lng: Double) {
    val uri = "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    context.startActivity(intent)
}
"""

new_open_google_maps = """
fun openGoogleMapsDirections(
    context: Context,
    lat: Double,
    lng: Double,
    destination: String? = null
) {
    try {
        val uri = Uri.Builder()
            .scheme("https")
            .authority("www.google.com")
            .path("/maps/dir/")
            .appendQueryParameter("api", "1")
            .appendQueryParameter("destination", "$lat,$lng")
            .appendQueryParameter("travelmode", "driving")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        val uri = Uri.parse("https://www.google.com/maps?q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}
"""

content = content.replace(old_open_google_maps.strip(), new_open_google_maps.strip())


with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(content)
