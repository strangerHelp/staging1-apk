import re

code = """package com.strangerhelp.app.ui.screens.feed

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.OnlinePrediction
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User

val PrimaryText = Color(0xFF000000)
val MutedText = Color(0xFF666666)
val OutlineColor = Color(0xFFE5E5E5)
val BgColor = Color(0xFFF9F9F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    user: User?,
    viewModel: FeedViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val recentTasks by viewModel.recentTasks.collectAsState()
    val pulseData by viewModel.pulseData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(user?.id) {
        viewModel.loadHomeData(user?.id)
    }

    Scaffold(
        containerColor = BgColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item {
                HomeHeader(user = user, unreadCount = 3) {
                    navController.navigate("notifications")
                }
            }

            item {
                StatsRow(stats = stats, city = user?.city ?: "Ban")
            }

            item {
                QuickActionsRow { action ->
                    when (action) {
                        "post" -> navController.navigate("post")
                        "pulse" -> navController.navigate("pulse")
                        "ask" -> navController.navigate("ask")
                        "browse" -> navController.navigate("tasks")
                    }
                }
            }

            item {
                ServicesSection { category ->
                    navController.navigate("tasks?category=$category")
                }
            }

            item {
                RecentTasksSection(
                    tasks = recentTasks,
                    isLoading = isLoading,
                    onTaskClick = { id -> navController.navigate("task/$id") }
                )
            }

            item {
                LivePulseMiniMap(
                    helperCount = pulseData.first.takeIf { it > 0 } ?: 12,
                    taskCount = pulseData.second.takeIf { it > 0 } ?: 8
                ) {
                    navController.navigate("pulse")
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(user: User?, unreadCount: Int, onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user?.avatar.takeIf { !it.isNullOrEmpty() } ?: "https://i.pravatar.cc/150?img=11",
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "👋 Welcome back,",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryText
                )
                Text(
                    text = "${user?.name?.split(" ")?.firstOrNull() ?: "Rakesh"}!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MutedText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user?.city ?: "Bangalore",
                        fontSize = 13.sp,
                        color = MutedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White,
                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                    ) { Text(unreadCount.toString()) }
                }
            },
            modifier = Modifier.clickable { onNotificationClick() }.padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MutedText, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun StatsRow(stats: UserStats, city: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(icon = Icons.Outlined.AddCircleOutline, iconTint = Color(0xFF8B5A2B), value = stats.tasksPosted.toString(), label = "Posted")
        StatCard(icon = Icons.Outlined.CheckBox, iconTint = Color(0xFFFF9800), value = stats.tasksClaimed.toString(), label = "Claimed")
        StatCard(icon = Icons.Outlined.CheckCircle, iconTint = PrimaryText, value = stats.tasksCompleted.toString(), label = "Done")
        StatCard(icon = Icons.Outlined.LocationCity, iconTint = PrimaryText, value = city.take(3), label = "City")
    }
}

@Composable
fun RowScope.StatCard(icon: ImageVector, iconTint: Color, value: String, label: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MutedText,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickActionsRow(onActionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Default.Add,
                iconTint = PrimaryText,
                label = "Post a Task",
                bgColor = Color(0xFFE5E5E5),
                textColor = PrimaryText,
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("post") }
            )
            QuickActionCard(
                icon = Icons.Outlined.OnlinePrediction,
                iconTint = Color(0xFF004D66),
                label = "Live Pulse",
                bgColor = Color(0xFFE0EDF2),
                textColor = Color(0xFF004D66),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("pulse") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Outlined.HelpOutline,
                iconTint = Color(0xFFCC7000),
                label = "Ask Question",
                bgColor = Color(0xFFFFF0E0),
                textColor = Color(0xFFCC7000),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("ask") }
            )
            QuickActionCard(
                icon = Icons.Default.Search,
                iconTint = PrimaryText,
                label = "Browse Tasks",
                bgColor = Color(0xFFE5E5E5),
                textColor = PrimaryText,
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("browse") }
            )
        }
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, iconTint: Color, label: String, bgColor: Color, textColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(96.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.elevatedCardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

data class ServiceCategory(val emoji: String, val name: String, val description: String)

@Composable
fun ServicesSection(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        ServiceCategory("📄", "Documents", "Drop off/Pick"),
        ServiceCategory("📦", "Parcel", "Courier"),
        ServiceCategory("🔧", "Repair", "Fix it"),
        ServiceCategory("🏠", "Cleaning", "Home Clean"),
        ServiceCategory("📸", "Photo", "Verification"),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("🔥", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Popular Services",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categories) { category ->
                ServiceCard(
                    emoji = category.emoji,
                    name = category.name,
                    description = category.description,
                    onClick = { onCategoryClick(category.name) }
                )
            }
        }
    }
}

@Composable
fun ServiceCard(emoji: String, name: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(116.dp)
            .height(136.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 46.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MutedText,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RecentTasksSection(tasks: List<Task>, isLoading: Boolean, onTaskClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
            Text("📋", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Your Recent Tasks",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }

        if (isLoading) {
            Text("Loading...", color = MutedText, fontSize = 14.sp)
        } else {
            val displayTasks = if (tasks.isEmpty()) {
                listOf(
                    Task(_id = "1", title = "Bike", description = "", category = "errand", budget = 1500, location = "", posterId = "", status = "open", urgent = 0, visibility = "public", createdAt = ""),
                    Task(_id = "2", title = "Collect report card...", description = "", category = "errand", budget = 1800, location = "", posterId = "", status = "open", urgent = 0, visibility = "public", createdAt = "")
                )
            } else tasks.take(2)

            displayTasks.forEach { task ->
                RecentTaskCard(task = task, onClick = { onTaskClick(task._id) })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RecentTaskCard(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (task.title.contains("Bike", ignoreCase = true)) Icons.Outlined.DirectionsBike else Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${task.budget}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "In Progress",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
fun LivePulseMiniMap(
    helperCount: Int,
    taskCount: Int,
    onMapClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Text("📡", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Live Pulse — $helperCount helpers online · $taskCount tasks nearby",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryText
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clickable { onMapClick() },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6F5)),
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw dots grid background
                    val dotSpacing = 16.dp.toPx()
                    for (x in 0..size.width.toInt() step dotSpacing.toInt()) {
                        for (y in 0..size.height.toInt() step dotSpacing.toInt()) {
                            drawCircle(color = Color(0xFFE0E0E0), radius = 2.5f, center = Offset(x.toFloat(), y.toFloat()))
                        }
                    }
                    
                    // Blurred dots
                    drawCircle(color = Color(0xFFFFCC80).copy(alpha = 0.7f), radius = 35f, center = Offset(size.width * 0.15f, size.height * 0.3f))
                    drawCircle(color = Color(0xFF90A4AE).copy(alpha = 0.6f), radius = 30f, center = Offset(size.width * 0.85f, size.height * 0.4f))
                    drawCircle(color = Color(0xFFFFCC80).copy(alpha = 0.6f), radius = 40f, center = Offset(size.width * 0.65f, size.height * 0.9f))
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(2.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tap to view full map", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = PrimaryText, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
"""

with open('app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt', 'w') as f:
    f.write(code)

