package com.strangerhelp.app.ui.screens.path

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.PlaceResult
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathSetupScreen(
    navController: NavController,
    viewModel: PathViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val path by viewModel.path.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isPathActive by viewModel.isPathActive.collectAsState()
    val matchedTasksCount by viewModel.matchedTasksCount.collectAsState()

    // Form state
    var fromQuery by remember { mutableStateOf("") }
    var fromSuggestions by remember { mutableStateOf<List<PlaceResult>>(emptyList()) }
    var selectedFrom by remember { mutableStateOf<PlaceResult?>(null) }

    var toQuery by remember { mutableStateOf("") }
    var toSuggestions by remember { mutableStateOf<List<PlaceResult>>(emptyList()) }
    var selectedTo by remember { mutableStateOf<PlaceResult?>(null) }

    var radiusKm by remember { mutableStateOf(1f) }
    var recurring by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }

    var scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadPath()
    }

    LaunchedEffect(fromQuery) {
        if (fromQuery.isNotEmpty() && fromQuery.length >= 3) {
            delay(400)
            val results = viewModel.searchPlaces(fromQuery)
            fromSuggestions = results
        } else {
            fromSuggestions = emptyList()
        }
    }

    LaunchedEffect(toQuery) {
        if (toQuery.isNotEmpty() && toQuery.length >= 3) {
            delay(400)
            val results = viewModel.searchPlaces(toQuery)
            toSuggestions = results
        } else {
            toSuggestions = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗺️ Path") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isPathActive) {
                        TextButton(
                            onClick = {
                                viewModel.deactivatePath()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Error
                            )
                        ) {
                            Text("End Path")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && path == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (isPathActive && path != null) {
            Column(modifier = Modifier.padding(padding)) {
                PathActiveScreen(
                    path = path!!,
                    tasks = tasks,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = TrustColor.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "🚀 Earn on your commute!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrustColor
                            )
                            Text(
                                "Set your route and find tasks along the way.",
                                fontSize = 12.sp,
                                color = Muted
                            )
                        }
                    }
                }

                // From Location
                item {
                    LocationAutocompleteField(
                        label = "From *",
                        query = fromQuery,
                        onQueryChange = { fromQuery = it; selectedFrom = null },
                        suggestions = fromSuggestions,
                        onSuggestionSelected = { result ->
                            selectedFrom = result
                            fromQuery = result.display_name
                            fromSuggestions = emptyList()
                        },
                        placeholder = "Koramangala, Bangalore"
                    )
                }

                // To Location
                item {
                    LocationAutocompleteField(
                        label = "To *",
                        query = toQuery,
                        onQueryChange = { toQuery = it; selectedTo = null },
                        suggestions = toSuggestions,
                        onSuggestionSelected = { result ->
                            selectedTo = result
                            toQuery = result.display_name
                            toSuggestions = emptyList()
                        },
                        placeholder = "Whitefield, Bangalore"
                    )
                }

                // Radius Slider
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Hairline),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "📏 Detour Radius",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "${"%.1f".format(radiusKm)} km",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark
                                )
                            }
                            Slider(
                                value = radiusKm,
                                onValueChange = { radiusKm = it },
                                valueRange = 0.5f..5f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryDark,
                                    activeTrackColor = PrimaryDark
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0.5 km", fontSize = 11.sp, color = Muted)
                                Text("5.0 km", fontSize = 11.sp, color = Muted)
                            }
                        }
                    }
                }

                // Recurring Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Hairline),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "🔄 Recurring Path",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Repeat daily for your commute",
                                    fontSize = 11.sp,
                                    color = Muted
                                )
                            }
                            Switch(
                                checked = recurring,
                                onCheckedChange = { recurring = it }
                            )
                        }
                    }
                }

                // Error
                if (error != null) {
                    item {
                        Text(error ?: "", color = Error)
                    }
                }

                // Find Tasks Button
                item {
                    Button(
                        onClick = {
                            if (selectedFrom != null && selectedTo != null) {
                                viewModel.setPath(
                                    fromLocation = selectedFrom!!.display_name,
                                    fromLat = selectedFrom!!.lat.toDouble(),
                                    fromLng = selectedFrom!!.lon.toDouble(),
                                    toLocation = selectedTo!!.display_name,
                                    toLat = selectedTo!!.lat.toDouble(),
                                    toLng = selectedTo!!.lon.toDouble(),
                                    radiusKm = radiusKm.toDouble(),
                                    recurring = recurring
                                ) { matched ->
                                    showMap = true
                                }
                            }
                        },
                        enabled = !isLoading && selectedFrom != null && selectedTo != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryDark
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("🗺️ Find Tasks Along My Route", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}
