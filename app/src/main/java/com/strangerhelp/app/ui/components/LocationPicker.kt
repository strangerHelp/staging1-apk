package com.strangerhelp.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strangerhelp.app.data.api.NominatimClient
import com.strangerhelp.app.data.api.NominatimResult
import com.strangerhelp.app.util.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LocationPicker(
    onLocationSelected: (lat: Double, lng: Double, address: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var selectedLat by remember { mutableDoubleStateOf(0.0) }
    var selectedLng by remember { mutableDoubleStateOf(0.0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    Column {
        // Search input
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                if (it.length >= 3) {
                    scope.launch {
                        delay(400) // Debounce
                        val res = NominatimClient.api.search(it)
                        if (res.isSuccessful) suggestions = res.body() ?: emptyList()
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
        TextButton(onClick = {
            scope.launch {
                val loc = LocationHelper(context).getCurrentLocation()
                if (loc != null) {
                    selectedLat = loc.latitude
                    selectedLng = loc.longitude
                    // Reverse geocode
                    val res = NominatimClient.api.reverse(loc.latitude, loc.longitude)
                    if (res.isSuccessful) {
                        query = res.body()?.display_name ?: "${loc.latitude}, ${loc.longitude}"
                        onLocationSelected(loc.latitude, loc.longitude, query)
                    } else {
                        query = "${loc.latitude}, ${loc.longitude}"
                        onLocationSelected(loc.latitude, loc.longitude, query)
                    }
                }
            }
        }) {
            Text("📍 Use my current location")
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
