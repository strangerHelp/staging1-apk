package com.strangerhelp.app.ui.screens.path

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

val PrimaryColor = Color(0xFFF59E0B) // Amber
val BackgroundColor = Color(0xFFF9F9F9)
val CardOutlineColor = Color(0xFFE5E5E5)
val TextColor = Color(0xFF111111)
val MutedText = Color(0xFF666666)
val BlueBg = Color(0xFFEBF8FF)
val CyanColor = Color(0xFF009688)
val CyanColorDeep = Color(0xFF00796B)
val Primary = Color(0xFFF59E0B)
val Error = Color(0xFFD32F2F)
val Warning = Color(0xFFF57C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathSetupScreen(
    navController: NavController,
    viewModel: PathViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var fromLocation by remember { mutableStateOf("Koramangala, Bangalore") }
    var fromLat by remember { mutableDoubleStateOf(12.9345) }
    var fromLng by remember { mutableDoubleStateOf(77.6123) }
    var toLocation by remember { mutableStateOf("Indiranagar, Bangalore") }
    var toLat by remember { mutableDoubleStateOf(12.9784) }
    var toLng by remember { mutableDoubleStateOf(77.6408) }
    var radiusKm by remember { mutableFloatStateOf(2.0f) }
    var recurring by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPath()
        viewModel.startAutoRefresh()
    }

    LaunchedEffect(viewModel.path.value) {
        if (viewModel.path.value != null) {
            navController.navigate("path_active") {
                popUpTo("path_setup") { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Set Path", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Map Placeholder
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardOutlineColor)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F6F5))) {
                            val dotSpacing = 16.dp.toPx()
                            for (x in 0..size.width.toInt() step dotSpacing.toInt()) {
                                for (y in 0..size.height.toInt() step dotSpacing.toInt()) {
                                    drawCircle(color = Color(0xFFE0E0E0), radius = 2.5f, center = Offset(x.toFloat(), y.toFloat()))
                                }
                            }
                            
                            val startX = size.width * 0.25f
                            val startY = size.height * 0.75f
                            val endX = size.width * 0.75f
                            val endY = size.height * 0.25f
                            
                            drawLine(
                                color = PrimaryColor.copy(alpha = 0.6f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 12f,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            
                            drawCircle(color = Color.White, radius = 20f, center = Offset(startX, startY))
                            drawCircle(color = Color.Black, radius = 10f, center = Offset(startX, startY))
                            
                            drawCircle(color = Color.White, radius = 20f, center = Offset(endX, endY))
                            drawCircle(color = PrimaryColor, radius = 10f, center = Offset(endX, endY))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Route Planner", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardOutlineColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // From
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.RadioButtonChecked, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("From", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextColor)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = fromLocation,
                            onValueChange = { fromLocation = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                Icon(Icons.Outlined.Explore, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = CardOutlineColor,
                                focusedBorderColor = PrimaryColor
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // To
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("To", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextColor)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = toLocation,
                            onValueChange = { toLocation = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = CardOutlineColor,
                                focusedBorderColor = PrimaryColor
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = CardOutlineColor)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Radius
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Explore, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Search Radius", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                            Text(
                                text = "${"%.1f".format(radiusKm)} km",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = radiusKm,
                            onValueChange = { radiusKm = it },
                            valueRange = 0.5f..5.0f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryColor,
                                activeTrackColor = PrimaryColor,
                                inactiveTrackColor = CardOutlineColor
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0.5 km", fontSize = 12.sp, color = MutedText)
                            Text("5.0 km", fontSize = 12.sp, color = MutedText)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Recurring
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardOutlineColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Recurring Path (daily)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                        Text(
                                            "Keep this route active every day.",
                                            fontSize = 13.sp,
                                            color = MutedText
                                        )
                                    }
                                }
                                Switch(
                                    checked = recurring,
                                    onCheckedChange = { recurring = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrimaryColor,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFD1D1D1)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (fromLocation.isNotEmpty() && toLocation.isNotEmpty()) {
                            viewModel.setPath(
                                fromLocation, fromLat, fromLng,
                                toLocation, toLat, toLng,
                                radiusKm.toDouble(), recurring
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && fromLocation.isNotEmpty() && toLocation.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.Explore, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Find Tasks Along My Route", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBE4FF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF0077B6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Path expires in 24 hours. You'll be notified of tasks along your route.",
                            fontSize = 14.sp,
                            color = Color(0xFF005580)
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
