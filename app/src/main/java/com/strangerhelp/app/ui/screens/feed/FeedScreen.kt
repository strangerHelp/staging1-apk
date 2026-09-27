package com.strangerhelp.app.ui.screens.feed

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.strangerhelp.app.ui.screens.notifications.NotificationViewModel
import com.strangerhelp.app.ui.screens.notifications.NotificationViewModelFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.strangerhelp.app.ui.components.TaskCardSkeleton
import com.strangerhelp.app.ui.components.EmptyState
import com.strangerhelp.app.data.model.User

val PrimaryText = Color(0xFF000000)
val MutedText = Color(0xFF666666)
val OutlineColor = Color(0xFFE5E5E5)
val BgColor = Color(0xFFFAF9F6) // slightly warmer off-white background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    user: User?,
    viewModel: FeedViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())
) {
    val stats by viewModel.stats.collectAsState()
    val recentTasks by viewModel.recentTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()

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
                HomeHeader(
                    user = user,
                    unreadCount = unreadCount,
                    onNotificationClick = { navController.navigate("notifications") },
                    onAvatarClick = { navController.navigate("profile") }
                )
            }
            
            item {
                // Search Bar to navigate to Tasks feed for filtering
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .clickable { navController.navigate("tasks") }
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Search tasks by keyword or category...", color = MutedText, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MutedText) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = false, // clicking it navigates to tasks tab
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = OutlineColor,
                            disabledContainerColor = Color.White,
                            disabledTextColor = PrimaryText,
                            disabledPlaceholderColor = MutedText,
                            disabledLeadingIconColor = MutedText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                StatsRow(
                    stats = stats, 
                    city = user?.city ?: "Ban",
                    onPostedClick = { navController.navigate("my_tasks?filter=posted") },
                    onClaimedClick = { navController.navigate("my_tasks?filter=claimed") },
                    onCompletedClick = { navController.navigate("my_tasks?filter=completed") },
                    onCityClick = { navController.navigate("edit_profile") }
                )
            }

            item {
                QuickActionsRow { action ->
                    when (action) {
                        "post" -> navController.navigate("post")
                        "pulse" -> navController.navigate("pulse")
                        "meets" -> navController.navigate("meets")
                        "path" -> navController.navigate("path_setup")
                    }
                }
            }

            item {
                ServicesSection { category ->
                    navController.navigate("tasks?category=$category")
                }
            }
            
            item {
                AskQuestionSection {
                    navController.navigate("ask")
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(
    user: User?,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    onAvatarClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            com.strangerhelp.app.ui.components.UserAvatar(
                avatarUrl = user?.avatar,
                name = user?.name,
                size = 52.dp,
                onClick = onAvatarClick
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
fun StatsRow(
    stats: UserStats, 
    city: String,
    onPostedClick: () -> Unit = {},
    onClaimedClick: () -> Unit = {},
    onCompletedClick: () -> Unit = {},
    onCityClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(icon = Icons.Outlined.AddCircleOutline, iconTint = Color(0xFF8B5A2B), value = stats.tasksPosted.toString(), label = "Posted", onClick = onPostedClick)
        StatCard(icon = Icons.Outlined.CheckBox, iconTint = Color(0xFFFF9800), value = stats.tasksClaimed.toString(), label = "Claimed", onClick = onClaimedClick)
        StatCard(icon = Icons.Outlined.CheckCircle, iconTint = PrimaryText, value = stats.tasksCompleted.toString(), label = "Done", onClick = onCompletedClick)
        StatCard(icon = Icons.Outlined.LocationCity, iconTint = PrimaryText, value = city.take(3), label = "City", onClick = onCityClick)
    }
}

@Composable
fun RowScope.StatCard(icon: ImageVector, iconTint: Color, value: String, label: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.animateContentSize().weight(1f).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                icon = Icons.Outlined.Sensors,
                iconTint = Color(0xFF005577),
                label = "Live Pulse",
                bgColor = Color(0xFFE0EDF2),
                textColor = Color(0xFF005577),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("pulse") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Outlined.Groups,
                iconTint = Color(0xFF8B5A2B),
                label = "Strangers Meet",
                bgColor = Color(0xFFFFF4E6),
                textColor = Color(0xFF8B5A2B),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("meets") }
            )
            QuickActionCard(
                icon = Icons.Outlined.Map,
                iconTint = Color(0xFF00897B),
                label = "Path",
                bgColor = Color(0xFFE0F2F1),
                textColor = Color(0xFF00897B),
                modifier = Modifier.weight(1f),
                onClick = { onActionClick("path") }
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
        modifier = Modifier.animateContentSize()
            .width(116.dp)
            .height(136.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
fun AskQuestionSection(onPostQuestionClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Text("❓", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ask Question",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }
        
        Card(
            modifier = Modifier.animateContentSize().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ask the community anything about your city",
                    fontSize = 15.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPostQuestionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Post Question",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { TaskCardSkeleton() }
            }
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
        modifier = Modifier.animateContentSize()
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                    val (statusColor, statusLabel) = when {
                        task.status.equals("completed", ignoreCase = true) || task.completionStatus.equals("approved", ignoreCase = true) ->
                            Pair(Color(0xFF10B981), "Completed")
                        task.status.equals("claimed", ignoreCase = true) ->
                            Pair(Color(0xFFF59E0B), "Claimed · In Progress")
                        task.status.equals("open", ignoreCase = true) ->
                            Pair(Color(0xFF0288D1), "Open")
                        else ->
                            Pair(Color(0xFF64748B), task.status.replaceFirstChar { it.uppercase() })
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                    if (task.category.isNotBlank()) {
                        Text(
                            text = " · ${task.category}",
                            fontSize = 13.sp,
                            color = MutedText
                        )
                    }
                }
            }
        }
    }
}
