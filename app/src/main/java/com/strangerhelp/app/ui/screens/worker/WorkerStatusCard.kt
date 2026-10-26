package com.strangerhelp.app.ui.screens.worker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Compact in-app banner that mirrors the persistent notification. Shown at
 * the top of the Home screen and Task Detail screen while the helper is online.
 */
@Composable
fun WorkerStatusCard(
    taskId: String = "",
    modifier: Modifier = Modifier,
    viewModel: WorkerStatusViewModel = viewModel()
) {
    val context = LocalContext.current
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val status by viewModel.currentStatus.collectAsStateWithLifecycle()

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val started = viewModel.goOnline(taskId)
            if (started) {
                Toast.makeText(context, "You are now online! Location sharing active.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Location permission is required to go online and share your position.", Toast.LENGTH_LONG).show()
        }
    }

    fun handleGoOnline() {
        if (!hasLocationPermission()) {
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            locationPermissionLauncher.launch(perms.toTypedArray())
        } else {
            val started = viewModel.goOnline(taskId)
            if (started) {
                Toast.makeText(context, "You are now online! Location sharing active.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Unable to start location service. Please verify permissions in Settings.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) Color(0xFF101828) else Color(0xFFF5F5F5)
        )
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
                    .background(
                        if (isOnline) Color(0xFF29BC9B) else Color(0xFF888888),
                        RoundedCornerShape(50)
                    )
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.title,
                    color = if (isOnline) Color.White else Color(0xFF171717),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = status.text,
                    color = if (isOnline) Color(0xFFCCCCCC) else Color(0xFF4D4D4D),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (isOnline) {
                TextButton(onClick = { viewModel.goOffline() }) {
                    Text("Stop", color = Color(0xFFF5A623))
                }
            } else {
                TextButton(onClick = { handleGoOnline() }) {
                    Text("Go online", color = Color(0xFF0070F3))
                }
            }
        }
    }
}
