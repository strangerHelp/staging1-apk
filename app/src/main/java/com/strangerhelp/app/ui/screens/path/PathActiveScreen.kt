package com.strangerhelp.app.ui.screens.path

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.data.model.Path
import com.strangerhelp.app.data.model.PathTask
import com.strangerhelp.app.ui.theme.Body
import com.strangerhelp.app.ui.theme.Error
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.TrustColor
import com.strangerhelp.app.ui.theme.Warning

@Composable
fun PathActiveScreen(
    path: Path,
    tasks: List<PathTask>,
    viewModel: PathViewModel,
    navController: NavController
) {
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Path Status Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = TrustColor.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(TrustColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "🟢 Path Active",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TrustColor
                        )
                    }
                    Text(
                        viewModel.getExpiryText(path),
                        fontSize = 12.sp,
                        color = Warning
                    )
                }

                Text(
                    "📍 ${path.fromLocation} → ${path.toLocation}",
                    fontSize = 13.sp,
                    color = Body
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "📏 ${"%.1f".format(path.radiusKm)} km radius",
                        fontSize = 12.sp,
                        color = Muted
                    )
                    if (path.recurring == 1) {
                        Text(
                            "🔄 Recurring",
                            fontSize = 12.sp,
                            color = Muted
                        )
                    }
                }
            }
        }

        // Route Map
        RouteMapView(
            fromLat = path.fromLat,
            fromLng = path.fromLng,
            toLat = path.toLat,
            toLng = path.toLng,
            tasks = tasks,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Tasks Count
        Text(
            text = "📊 ${tasks.size} tasks along your route",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Task List
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📍", fontSize = 48.sp)
                    Text(
                        "No tasks along your route",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Try increasing your detour radius",
                        fontSize = 13.sp,
                        color = Muted
                    )
                    Button(
                        onClick = {
                            viewModel.deactivatePath()
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Adjust Route")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    PathTaskCard(
                        task = task,
                        onClick = {
                            navController.navigate("task_detail/${task.id}")
                        }
                    )
                }
            }
        }

        // End Path Button
        Button(
            onClick = { viewModel.deactivatePath() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Error
            ),
            shape = RoundedCornerShape(26.dp),
            enabled = !isLoading
        ) {
            Text("🗑️ End Path", color = MaterialTheme.colorScheme.onError)
        }
    }
}
