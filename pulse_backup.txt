package com.strangerhelp.app.ui.screens.pulse

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.strangerhelp.app.data.model.HelpRequest
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import kotlin.random.Random
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.expressions.Expression.*

data class MapPin(val location: LatLng, val isHelper: Boolean)

fun getFeatureCollection(pins: List<MapPin>): FeatureCollection {
    val features = pins.map { pin ->
        val feature = Feature.fromGeometry(Point.fromLngLat(pin.location.longitude, pin.location.latitude))
        feature.addBooleanProperty("isHelper", pin.isHelper)
        feature.addStringProperty("type", if (pin.isHelper) "helper" else "task")
        feature
    }
    return FeatureCollection.fromFeatures(features)
}

fun createMarkerBitmap(isHelper: Boolean): android.graphics.Bitmap {
    val size = 64
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    
    paint.color = if (isHelper) android.graphics.Color.parseColor("#00E676") else android.graphics.Color.parseColor("#FF9800")
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
    
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 6f
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
    
    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PulseScreen(navController: NavController) {
    var isOnline by remember { mutableStateOf(false) }
    val db = com.strangerhelp.app.StrangerHelpApp.instance.database
    val helpRequests by db.helpRequestDao().getAllHelpRequests().collectAsStateWithLifecycle(initialValue = emptyList())
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        isLoading = false
    }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    // Request permission on launch if not granted
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Sync online state with permission
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (!locationPermissionState.status.isGranted && isOnline) {
            isOnline = false
        }
    }

    val context = LocalContext.current
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    
    // Example pins
    val centerPoint = LatLng(28.6139, 77.2090)
    val pins = remember {
        List(15) {
            MapPin(
                location = LatLng(28.6139 + (Random.nextDouble() - 0.5) * 0.1, 77.2090 + (Random.nextDouble() - 0.5) * 0.1),
                isHelper = Random.nextBoolean()
            )
        }
    }
    
    // Ensure MapLibre is initialized
    LaunchedEffect(Unit) {
        try {
            com.strangerhelp.app.util.MapHelper.initMap(context)
            val offlineManager = OfflineManager.getInstance(context)
            offlineManager.setMaximumAmbientCacheSize(150 * 1024 * 1024, object : OfflineManager.FileSourceCallback {
                override fun onSuccess() {}
                override fun onError(message: String) {}
            })
        } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pulse") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    com.strangerhelp.app.util.MapHelper.initMap(ctx)
                    MapView(ctx).apply {
                        getMapAsync { maplibreMap ->
                            mapRef = maplibreMap
                            maplibreMap.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                                style.addImage("marker-helper", createMarkerBitmap(true))
                                style.addImage("marker-task", createMarkerBitmap(false))
                                
                                val geoJsonSource = GeoJsonSource("pins-source", getFeatureCollection(pins))
                                style.addSource(geoJsonSource)
                                
                                val symbolLayer = SymbolLayer("pins-layer", "pins-source")
                                symbolLayer.setProperties(
                                    iconImage(switchCase(get("isHelper"), literal("marker-helper"), literal("marker-task"))),
                                    iconAllowOverlap(true),
                                    iconIgnorePlacement(true),
                                    iconSize(
                                        interpolate(
                                            exponential(1.5f), zoom(),
                                            stop(10f, 0.5f),
                                            stop(15f, 1.0f),
                                            stop(18f, 2.0f)
                                        )
                                    )
                                )
                                style.addLayer(symbolLayer)
                            }
                            
                            val position = CameraPosition.Builder()
                                .target(centerPoint)
                                .zoom(13.0)
                                .build()
                            maplibreMap.cameraPosition = position
                        }
                    }
                },
                update = { mapView ->
                    mapView.getMapAsync { map ->
                        map.getStyle { style ->
                            val source = style.getSourceAs<GeoJsonSource>("pins-source")
                            source?.setGeoJson(getFeatureCollection(pins))
                        }
                    }
                }
            )

            // Zoom controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        mapRef?.let { map ->
                            val currentZoom = map.cameraPosition.zoom
                            map.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1.0))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Filled.Add, "Zoom In")
                }
                SmallFloatingActionButton(
                    onClick = {
                        mapRef?.let { map ->
                            val currentZoom = map.cameraPosition.zoom
                            map.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1.0))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Filled.Remove, "Zoom Out")
                }
            }

            // Top overlay card
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .fillMaxWidth(0.8f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(CyanDeep, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("Live Pulse", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Text("${pins.count { it.isHelper }} online", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Real-time view of helpers and open tasks near you.", style = MaterialTheme.typography.bodySmall, color = Muted)
                    
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Go Online as Helper", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Share location on map", style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { checked -> 
                                if (checked && !locationPermissionState.status.isGranted) {
                                    locationPermissionState.launchPermissionRequest()
                                } else {
                                    isOnline = checked
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF00E676), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text("Helpers", style = MaterialTheme.typography.labelSmall, color = Muted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF9800), CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text("Open Tasks", style = MaterialTheme.typography.labelSmall, color = Muted)
                        }
                    }
                }
            }

            // Bottom overlay card (Open Tasks list)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp) // Leave space for FAB
                    .fillMaxWidth(0.85f)
                    .heightIn(max = 200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Open Tasks Nearby", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (helpRequests.isEmpty()) {
                        Text("No open tasks nearby.", style = MaterialTheme.typography.bodySmall, color = Muted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(helpRequests) { request ->
                                Column {
                                    Text(request.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("${request.location} • ₹150", style = MaterialTheme.typography.bodySmall, color = Link)
                                    HorizontalDivider(Modifier.padding(top = 8.dp), color = Hairline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
