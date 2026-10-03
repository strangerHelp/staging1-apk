package com.strangerhelp.app.ui.screens.tasks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.strangerhelp.app.data.api.NominatimClient
import com.strangerhelp.app.data.api.NominatimResult
import com.strangerhelp.app.ui.theme.PrimaryDark
import com.strangerhelp.app.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

data class PresetCity(val name: String, val lat: Double?, val lng: Double?)

val PRESET_CITIES = listOf(
    PresetCity("All Locations", null, null),
    PresetCity("Bangalore", 12.9716, 77.5946),
    PresetCity("Mumbai", 19.0760, 72.8777),
    PresetCity("Delhi NCR", 28.6139, 77.2090),
    PresetCity("Hyderabad", 17.3850, 78.4867),
    PresetCity("Chennai", 13.0827, 80.2707),
    PresetCity("Pune", 18.5204, 73.8567),
    PresetCity("Kolkata", 22.5726, 88.3639)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationChangeDialog(
    currentCity: String,
    onDismiss: () -> Unit,
    onLocationSelected: (locationName: String, lat: Double?, lng: Double?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var manualInput by remember { mutableStateOf("") }
    var isDetectingLocation by remember { mutableStateOf(false) }
    var isSearchingSuggestions by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    fun requestGpsLocation() {
        isDetectingLocation = true
        errorMessage = null
        infoMessage = "Getting your current GPS location..."

        scope.launch {
            try {
                val loc = withTimeoutOrNull(8000L) {
                    LocationHelper(context).getCurrentLocation()
                }
                if (loc != null) {
                    val placeName = resolveLocationName(context, loc.latitude, loc.longitude)
                    onLocationSelected(placeName, loc.latitude, loc.longitude)
                    onDismiss()
                } else {
                    errorMessage = "Unable to retrieve GPS coordinates. Please enter your location manually below."
                    infoMessage = null
                }
            } catch (e: Exception) {
                errorMessage = "Location error: ${e.localizedMessage ?: "Failed to get location"}"
                infoMessage = null
            } finally {
                isDetectingLocation = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            requestGpsLocation()
        } else {
            errorMessage = "Location permission was denied. You can still search or select your city manually below."
            infoMessage = null
        }
    }

    fun handleUseCurrentLocationClick() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            requestGpsLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Debounce manual search suggestions
    LaunchedEffect(manualInput) {
        val query = manualInput.trim()
        if (query.length >= 3) {
            delay(350)
            isSearchingSuggestions = true
            try {
                val res = NominatimClient.api.search(query)
                if (res.isSuccessful) {
                    suggestions = res.body().orEmpty()
                }
            } catch (_: Exception) {
                // Ignore search network exceptions
            } finally {
                isSearchingSuggestions = false
            }
        } else {
            suggestions = emptyList()
            isSearchingSuggestions = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .testTag("location_change_dialog"),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AccentOrange.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Change Location",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                            Text(
                                "Current: $currentCity",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_location_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // GPS Button Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isDetectingLocation) { handleUseCurrentLocationClick() }
                        .testTag("use_gps_location_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFDCFCE7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDetectingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFF16A34A),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.MyLocation,
                                        contentDescription = "Current Location",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Use Current GPS Location",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    if (isDetectingLocation) "Detecting location..." else "Find tasks closest to your current spot",
                                    fontSize = 12.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Status / Error Messages
                if (errorMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(errorMessage.orEmpty(), fontSize = 12.sp, color = Color(0xFFB91C1C), lineHeight = 16.sp)
                        }
                    }
                }

                if (infoMessage != null && errorMessage == null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF2563EB), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(infoMessage.orEmpty(), fontSize = 12.sp, color = Color(0xFF1D4ED8))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Divider with OR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                    Text(
                        "  OR ENTER MANUALLY  ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                }

                Spacer(Modifier.height(14.dp))

                // Manual Input
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    placeholder = { Text("Search city, area, or pin code...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (manualInput.isNotEmpty()) {
                            IconButton(onClick = { manualInput = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_location_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (manualInput.isNotBlank()) {
                            onLocationSelected(manualInput.trim(), null, null)
                            onDismiss()
                        }
                    })
                )

                // Suggestions list if available
                if (isSearchingSuggestions) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentOrange, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Searching locations...", fontSize = 12.sp, color = Color.Gray)
                    }
                } else if (suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .heightIn(max = 160.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(suggestions) { item ->
                                val cleanName = item.display_name.split(",").take(3).joinToString(", ")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val lat = item.lat.toDoubleOrNull()
                                            val lon = item.lon.toDoubleOrNull()
                                            val primaryName = item.display_name.split(",").firstOrNull()?.trim() ?: cleanName
                                            onLocationSelected(primaryName, lat, lon)
                                            onDismiss()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.LocationOn, null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(cleanName, fontSize = 12.sp, color = PrimaryDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                HorizontalDivider(color = Color(0xFFF3F4F6))
                            }
                        }
                    }
                }

                // If user typed something and wants to apply it directly
                if (manualInput.isNotBlank() && suggestions.isEmpty() && !isSearchingSuggestions) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onLocationSelected(manualInput.trim(), null, null)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("apply_manual_location_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                    ) {
                        Text("Search tasks in \"${manualInput.trim()}\"", fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Popular Cities Row
                Text(
                    "Popular Cities",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PRESET_CITIES) { city ->
                        val isSelected = currentCity.equals(city.name, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) PrimaryDark else Color(0xFFF3F4F6),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier
                                .clickable {
                                    onLocationSelected(city.name, city.lat, city.lng)
                                    onDismiss()
                                }
                                .testTag("preset_city_${city.name.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = city.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else PrimaryDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

suspend fun resolveLocationName(context: Context, lat: Double, lng: Double): String {
    // 1. Android Geocoder
    try {
        @Suppress("DEPRECATION")
        val addresses = withTimeoutOrNull(2500L) {
            withContext(Dispatchers.IO) {
                Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
            }
        }
        val addr = addresses?.firstOrNull()
        if (addr != null) {
            val locality = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: addr.adminArea
            if (!locality.isNullOrBlank()) {
                val subLoc = addr.subLocality
                return if (!subLoc.isNullOrBlank() && subLoc != locality) {
                    "$subLoc, $locality"
                } else {
                    locality
                }
            }
        }
    } catch (_: Exception) {}

    // 2. Nominatim reverse geocoding fallback
    try {
        val res = withTimeoutOrNull(2500L) {
            NominatimClient.api.reverse(lat, lng)
        }
        if (res != null && res.isSuccessful) {
            val displayName = res.body()?.display_name
            if (!displayName.isNullOrBlank()) {
                val parts = displayName.split(",").map { it.trim() }.filter { it.isNotBlank() }
                return parts.take(2).joinToString(", ")
            }
        }
    } catch (_: Exception) {}

    // 3. Fallback coordinate label
    return "Nearby (%.3f, %.3f)".format(lat, lng)
}
