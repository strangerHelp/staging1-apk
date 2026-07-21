package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(navController: NavController, user: User) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Simple Survey", "Task", "Document Submission", "Photo Proof", "Parcel Pickup", "Queue Standing", "Verification", "Receipt Collection", "Other")

    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.api.getTasks() // Get all tasks
            if (res.isSuccessful) tasks = res.body() ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    val filteredTasks = if (selectedCategory == "All") tasks else tasks.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Tasks", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Text("Find tasks near you", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Surface(
                    onClick = { selectedCategory = category },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Hairline),
                    modifier = Modifier.height(32.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
            }
        } else if (filteredTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No tasks found. Be the first to post one!", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredTasks) { task ->
                    TaskCard(task = task, onClick = { navController.navigate("task/${task._id}") })
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = SurfaceVariant,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = task.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Body
                    )
                }
                if (task.urgent == 1) {
                    Surface(
                        color = Color(0xFFF7D4D6),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "⚡ Urgent",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Error
                        )
                    }
                }
            }
            Text(
                text = "₹${task.budget}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Muted)
                Text(
                    text = task.location,
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp)
                )
            }
            
            if (task.distance != null) {
                val dist = if (task.distance < 1) "${(task.distance * 1000).toInt()} m" else "%.1f km".format(task.distance)
                Text(text = "$dist away", style = MaterialTheme.typography.labelMedium, color = Link, fontWeight = FontWeight.Medium)
            } else {
                Text(text = task.createdAt.take(10), style = MaterialTheme.typography.labelMedium, color = Muted)
            }
        }

        Spacer(Modifier.height(12.dp))
        Divider(color = Hairline)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (task.anonymous == 1) "Anonymous" else (task.posterId.take(8)), // Using posterId as name placeholder since we don't have posterName in Task model
                    style = MaterialTheme.typography.labelMedium,
                    color = Body
                )
                if (task.posterVerified) {
                    Text(text = "✓ Verified", style = MaterialTheme.typography.labelSmall, color = Link, fontSize = 10.sp)
                }
            }

            val statusColor = when (task.status) {
                "open" -> CyanDeep
                "claimed" -> Warning
                else -> Muted
            }
            val statusBgColor = when (task.status) {
                "open" -> Color(0xFFAAFFEC)
                "claimed" -> Color(0xFFFFEFCF)
                else -> SurfaceVariant
            }

            Surface(
                color = statusBgColor,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = task.status.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }
    }
}
