import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/components/LocationPicker.kt"
with open(file_path, "r") as f:
    content = f.read()

new_code = """package com.strangerhelp.app.ui.components

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.strangerhelp.app.data.api.NominatimClient
import com.strangerhelp.app.data.api.NominatimResult
import com.strangerhelp.app.util.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPicker(
    onLocationSelected: (lat: Double, lng: Double, address: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var selectedLat by remember { mutableDoubleStateOf(0.0) }
    var selectedLng by remember { mutableDoubleStateOf(0.0) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    Column {
        // Search input
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onLocationSelected(0.0, 0.0, it)
                if (it.length >= 3) {
                    scope.launch {
                        delay(400) // Debounce
                        try {
                            val res = NominatimClient.api.search(it)
                            if (res.isSuccessful) suggestions = res.body() ?: emptyList()
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                } else {
                    suggestions = emptyList()
                }
            },
            placeholder = { Text("Search location...") },
            leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
            modifier = Modifier.fillMaxWidth()
        )

        // "Use my location" button
        TextButton(
            onClick = {
                if (locationPermissionsState.allPermissionsGranted) {
                    isLoadingLocation = true
                    scope.launch {
                        val loc = LocationHelper(context).getCurrentLocation()
                        if (loc != null) {
                            selectedLat = loc.latitude
                            selectedLng = loc.longitude
                            // Reverse geocode
                            try {
                                val res = NominatimClient.api.reverse(loc.latitude, loc.longitude)
                                if (res.isSuccessful) {
                                    query = res.body()?.display_name ?: "${loc.latitude}, ${loc.longitude}"
                                    onLocationSelected(loc.latitude, loc.longitude, query)
                                } else {
                                    query = "${loc.latitude}, ${loc.longitude}"
                                    onLocationSelected(loc.latitude, loc.longitude, query)
                                }
                            } catch (e: Exception) {
                                query = "${loc.latitude}, ${loc.longitude}"
                                onLocationSelected(loc.latitude, loc.longitude, query)
                            }
                        }
                        isLoadingLocation = false
                    }
                } else {
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            },
            enabled = !isLoadingLocation
        ) {
            if (isLoadingLocation) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fetching location...")
            } else {
                Text("📍 Use my current location")
            }
        }

        if (locationPermissionsState.shouldShowRationale) {
            Text(
                "Location permission is needed to accurately set your task location.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Suggestions dropdown
        suggestions.forEach { result ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val lat = result.lat.toDoubleOrNull() ?: 0.0
                        val lng = result.lon.toDoubleOrNull() ?: 0.0
                        query = result.display_name
                        suggestions = emptyList()
                        onLocationSelected(lat, lng, result.display_name)
                    }
            ) {
                Text(
                    result.display_name,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
"""

with open(file_path, "w") as f:
    f.write(new_code)
