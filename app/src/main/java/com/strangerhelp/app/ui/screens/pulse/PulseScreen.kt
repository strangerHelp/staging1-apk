package com.strangerhelp.app.ui.screens.pulse

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.strangerhelp.app.data.model.HelpRequest
import com.strangerhelp.app.ui.theme.*
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import com.strangerhelp.app.util.MapHelper
import com.strangerhelp.app.utils.BatteryMonitor
import org.maplibre.android.maps.Style
import kotlin.random.Random
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.maplibre.android.style.layers.PropertyFactory.*

data class MapPin(val location: LatLng, val isHelper: Boolean)

fun getFeatureCollection(pins: List<MapPin>): FeatureCollection {
    val features = pins.map { pin ->
        val feature = Feature.fromGeometry(Point.fromLngLat(pin.location.longitude, pin.location.latitude))
        feature.addStringProperty("type", if (pin.isHelper) "helper" else "task")
        feature
    }
    return FeatureCollection.fromFeatures(features)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PulseScreen(navController: NavController) {
    val context = LocalContext.current
    val db = com.strangerhelp.app.StrangerHelpApp.instance.database
    val helpRequests by db.helpRequestDao().getAllHelpRequests().collectAsStateWithLifecycle(initialValue = emptyList())
    var isLoading by remember { mutableStateOf(true) }
    val downloadProgress by MapHelper.offlineDownloadProgress.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { kotlinx.coroutines.delay(1500); isLoading = false }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    val isBatterySaver by BatteryMonitor.isBatterySaverMode.collectAsStateWithLifecycle()
    
    // Simulating map refresh frequency adapting to battery state
    var pins by remember {
        mutableStateOf(List(15) {
            MapPin(
                location = LatLng(28.6139 + (Random.nextDouble() - 0.5) * 0.1, 77.2090 + (Random.nextDouble() - 0.5) * 0.1),
                isHelper = Random.nextBoolean()
            )
        })
    }
    LaunchedEffect(isBatterySaver) {
        while(true) {
            val delayMillis = if (isBatterySaver) 30000L else 10000L
            kotlinx.coroutines.delay(delayMillis)
            
            // Refresh map pins
            pins = List(15) {
                MapPin(
                    location = LatLng(28.6139 + (Random.nextDouble() - 0.5) * 0.1, 77.2090 + (Random.nextDouble() - 0.5) * 0.1),
                    isHelper = Random.nextBoolean()
                )
            }
        }
    }
    
    var isOnline by remember { mutableStateOf(false) }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    
    val centerPoint = LatLng(28.6139, 77.2090)

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
            Column {
                TopAppBar(
                    title = { Text("Live Pulse", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Refresh */ }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A9D8F))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ 12 helpers online · 8 tasks nearby",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        getMapAsync { maplibreMap ->
                            mapRef = maplibreMap
                            maplibreMap.setStyle(Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")) { style ->
                                val helperBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
                                    val canvas = Canvas(this)
                                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#00BCD4") }
                                    canvas.drawCircle(24f, 24f, 20f, paint)
                                    paint.color = AndroidColor.WHITE
                                    paint.style = Paint.Style.STROKE
                                    paint.strokeWidth = 4f
                                    canvas.drawCircle(24f, 24f, 20f, paint)
                                }
                                style.addImage("helper-marker", helperBitmap)

                                val taskBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
                                    val canvas = Canvas(this)
                                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#FF9800") }
                                    canvas.drawCircle(24f, 24f, 20f, paint)
                                    paint.color = AndroidColor.WHITE
                                    paint.style = Paint.Style.STROKE
                                    paint.strokeWidth = 4f
                                    canvas.drawCircle(24f, 24f, 20f, paint)
                                }
                                style.addImage("task-marker", taskBitmap)

                                val source = GeoJsonSource("pins-source", getFeatureCollection(pins))
                                style.addSource(source)

                                val symbolLayer = SymbolLayer("pins-layer", "pins-source").withProperties(
                                    iconImage(
                                        org.maplibre.android.style.expressions.Expression.match(
                                            org.maplibre.android.style.expressions.Expression.get("type"),
                                            org.maplibre.android.style.expressions.Expression.literal("helper-marker"),
                                            org.maplibre.android.style.expressions.Expression.stop("helper", "helper-marker"),
                                            org.maplibre.android.style.expressions.Expression.stop("task", "task-marker")
                                        )
                                    ),
                                    iconAllowOverlap(true),
                                    iconIgnorePlacement(true),
                                    iconSize(
                                        org.maplibre.android.style.expressions.Expression.interpolate(
                                            org.maplibre.android.style.expressions.Expression.exponential(1.5f), org.maplibre.android.style.expressions.Expression.zoom(),
                                            org.maplibre.android.style.expressions.Expression.stop(10f, 0.5f),
                                            org.maplibre.android.style.expressions.Expression.stop(15f, 1.0f),
                                            org.maplibre.android.style.expressions.Expression.stop(18f, 2.0f)
                                        )
                                    )
                                )
                                style.addLayer(symbolLayer)
                            }
                            
                            val position = CameraPosition.Builder()
                                .target(centerPoint)
                                .zoom(12.0)
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
            
            if (downloadProgress != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Downloading Offline Map...", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress!! / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = Saffron
                        )
                    }
                }
            }

            // Map Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 120.dp),
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
                SmallFloatingActionButton(
                    onClick = {
                        mapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 13.0))
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Outlined.MyLocation, "Locate")
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        MapHelper.downloadOfflineRegion(context, centerPoint, radiusKm = 10.0, regionName = "PulseRegion")
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Outlined.CloudDownload, "Download Offline Map")
                }
            }

            // Bottom UI
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Go Online Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFFFF3E0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Storefront, contentDescription = null, tint = Color(0xFFFF9800))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Go Online as Helper", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("Receive nearby task requests", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
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
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2A9D8F),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = "Recent Pulse Activity",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Horizontal Scrollable Cards
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.width(200.dp).height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Article, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    Spacer(Modifier.width(4.dp))
                                    Text("New Task", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Submit Document", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.weight(1f))
                                Text("₹500", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF9800))
                            }
                        }
                    }
                    item {
                        Card(
                            modifier = Modifier.width(200.dp).height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Helper Online", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("John D. is nearby", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.weight(1f))
                                Text("0.2 km away", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2A9D8F))
                            }
                        }
                    }
                }
            }
        }
    }
}
