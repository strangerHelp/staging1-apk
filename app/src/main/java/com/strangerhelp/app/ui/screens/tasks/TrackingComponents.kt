package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import org.maplibre.android.maps.MapView
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.MarkerOptions
import com.strangerhelp.app.util.MapHelper
import org.maplibre.android.camera.CameraPosition



import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.ui.theme.*
import kotlin.math.*
import android.os.Bundle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

fun haversineKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val R = 6371.0 
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

@Composable
fun HelperTrackingControls(
    task: Task,
    viewModel: TaskDetailViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val trackingError by viewModel.trackingError.collectAsState()
    var showDisclosure by remember { mutableStateOf(false) }

    val TrustColor = Color(0xFF10B981) // Green
    val Body = Color.Gray
    val Muted = Color.LightGray
    val Error = Color.Red

    val requiredPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result[Manifest.permission.POST_NOTIFICATIONS] == true
        } else true

        if ((fineGranted || coarseGranted) && notifGranted) {
            viewModel.startTracking(task._id, context)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            isTracking -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = TrustColor.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(TrustColor)
                                    .animateContentSize()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "📍 Live tracking active",
                                fontSize = 13.sp,
                                color = TrustColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = { viewModel.stopTracking(task._id, context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Error
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                "Stop",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            else -> {
                OutlinedButton(
                    onClick = { showDisclosure = true },
                    modifier = modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TrustColor
                    ),
                    border = BorderStroke(1.dp, TrustColor)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = TrustColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "📍 Start Task (Share Live Location)",
                        color = TrustColor
                    )
                }
            }
        }

        trackingError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = { Text("Share Your Location") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "StrangerHelp will share your live location with the task poster while you complete this task.",
                        fontSize = 14.sp,
                        color = Body
                    )
                    Text(
                        "You can stop sharing anytime.",
                        fontSize = 13.sp,
                        color = Muted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisclosure = false
                        permissionLauncher.launch(requiredPermissions)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrustColor
                    )
                ) {
                    Text("Start Sharing", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun PosterTrackingView(
    task: Task,
    modifier: Modifier = Modifier
) {
    if (!task.trackingActive || task.helperLat == null || task.helperLng == null) {
        return
    }

    val TrustColor = Color(0xFF10B981)
    val Body = Color.Gray

    val distance = haversineKm(
        task.helperLat,
        task.helperLng,
        task.lat ?: 0.0,
        task.lng ?: 0.0
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = TrustColor.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(TrustColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "📍 Live Tracking Active",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrustColor
                    )
                    Text(
                        "Helper is ${"%.1f".format(distance)} km from task location",
                        fontSize = 12.sp,
                        color = Body
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            HelperLocationMap(
                helperLat = task.helperLat,
                helperLng = task.helperLng,
                taskLat = task.lat ?: 0.0,
                taskLng = task.lng ?: 0.0
            )
        }
    }
}

@Composable
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
}