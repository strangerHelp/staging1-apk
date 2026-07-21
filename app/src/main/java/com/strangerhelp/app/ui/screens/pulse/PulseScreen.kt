package com.strangerhelp.app.ui.screens.pulse

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MapPin(val x: Float, val y: Float, val isHelper: Boolean)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PulseScreen(navController: NavController) {
    var isOnline by remember { mutableStateOf(false) }
    
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
    
    // Generate some random pins for the map
    val pins = remember {
        List(15) {
            MapPin(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                isHelper = Random.nextBoolean()
            )
        }
    }

    // Animation for pulsing dots
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Pulse", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("post") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Post a Task")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Simulated Map Background
            Canvas(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                val width = size.width
                val height = size.height
                
                // Draw grid
                val gridSize = 50.dp.toPx()
                for (i in 0..(width / gridSize).toInt()) {
                    drawLine(Hairline, Offset(i * gridSize, 0f), Offset(i * gridSize, height), strokeWidth = 1f)
                }
                for (i in 0..(height / gridSize).toInt()) {
                    drawLine(Hairline, Offset(0f, i * gridSize), Offset(width, i * gridSize), strokeWidth = 1f)
                }

                // Draw pins
                pins.forEach { pin ->
                    val color = if (pin.isHelper) CyanDeep else Color(0xFFFF9800) // Warning color for tasks
                    val center = Offset(pin.x * width, pin.y * height)
                    
                    // Draw pulse
                    drawCircle(
                        color = color.copy(alpha = pulseAlpha),
                        radius = pulseRadius * 2,
                        center = center
                    )
                    // Draw solid center
                    drawCircle(
                        color = color,
                        radius = 6.dp.toPx(),
                        center = center
                    )
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
                            Box(modifier = Modifier.size(8.dp).background(CyanDeep, CircleShape))
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
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(3) {
                            Column {
                                Text("Need help carrying groceries", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("500m away • ₹150", style = MaterialTheme.typography.bodySmall, color = Link)
                                if (it < 2) HorizontalDivider(Modifier.padding(top = 8.dp), color = Hairline)
                            }
                        }
                    }
                }
            }
        }
    }
}
