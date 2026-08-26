package com.strangerhelp.app.utils

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionRequest(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            onGranted()
        }
    }

    when {
        locationPermissionState.status.isGranted -> {
            onGranted()
        }
        locationPermissionState.status.shouldShowRationale -> {
            // Show explanation why location is needed
            AlertDialog(
                onDismissRequest = { /* Don't dismiss */ },
                title = { Text("Location Permission Needed") },
                text = { Text("StrangerHelp uses your location to show tasks near you and provide directions.") },
                confirmButton = {
                    Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                        Text("Allow Location")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDenied) {
                        Text("Skip")
                    }
                }
            )
        }
        else -> {
            // Permission permanently denied
            Text(
                "Location permission is required for this feature. Please enable it in settings.",
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }) {
                Text("Open Settings")
            }
        }
    }
}
