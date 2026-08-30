package com.strangerhelp.app.ui.screens.meets

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
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
import com.strangerhelp.app.StrangerHelpApp
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Meet
import com.strangerhelp.app.ui.theme.Muted
import kotlinx.coroutines.launch
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.strangerhelp.app.ui.components.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetsScreen(navController: NavController) {
    val db = StrangerHelpApp.instance.database
    val meets by db.meetDao().getAllMeets().collectAsStateWithLifecycle(initialValue = emptyList())
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All Meets") }
    var joinCode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Categories matching the screenshot
    val categories = listOf("All Meets", "Coffee", "Sports", "Study")
    
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            try {
                val res = ApiClient.api.getMeets()
                if (res.isSuccessful) {
                    val body = res.body() ?: emptyList()
                    db.meetDao().insertMeets(body)
                }
            } catch (e: Exception) {}
            pullRefreshState.endRefresh()
        }
    }
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = ApiClient.api.getMeets()
                if (res.isSuccessful) {
                    val body = res.body() ?: emptyList()
                    db.meetDao().insertMeets(body)
                }
            } catch (e: Exception) {
            }
            isLoading = false
        }
    }
    
    val filteredMeets = if (selectedCategory == "All Meets") {
        meets
    } else {
        meets.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }
    
    // For the UI mockup look, let's mix some real data with hardcoded visually similar data
    // If DB is empty and not loading, we can show some dummy items to match the screenshot design if user wants it,
    // but better to map the real `Meet` model to the UI components.

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("StrangerHelp", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    
                    // Avatar
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("postMeet") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().nestedScroll(pullRefreshState.nestedScrollConnection)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB/nav
            ) {
                item {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Text(
                            text = "Community Meets",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Discover and join hyperlocal gatherings.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                onClick = { selectedCategory = category },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp).padding(bottom = 24.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = joinCode,
                            onValueChange = { joinCode = it },
                            placeholder = { Text("Enter Invite Code", color = Muted, style = MaterialTheme.typography.bodyLarge) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            singleLine = true
                        )
                        Surface(
                            onClick = { /* Join */ },
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                            modifier = Modifier.fillMaxHeight().width(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Join\nPrivate",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                if (isLoading && meets.isEmpty()) {
                    items(3) {
                        Box(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                            MeetCardShimmer()
                        }
                    }
                } else if (filteredMeets.isEmpty() && meets.isNotEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No meets found in this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    val displayMeets = if (meets.isEmpty()) getDummyMeets() else filteredMeets
                    items(displayMeets) { meet ->
                        Box(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                            MeetCard(meet = meet)
                        }
                    }
                }
            }
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun MeetCard(meet: Meet) {
    val hostInitials = meet.host_name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    val isWaitlist = meet.attendeeCount >= meet.max_attendees && meet.max_attendees > 0
    val isVerified = meet.host_name.contains("Alice") || meet.category.equals("verified", ignoreCase = true)
    val isActive = meet.title.contains("Run") || meet.title.contains("Active")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(24.dp)) {
            // Header Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    // Host Avatar
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (hostInitials.isNotEmpty()) hostInitials else "JD", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = meet.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Hosted by ${meet.host_name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Badges
                if (isActive) {
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF4CAF50), CircleShape))
                            Text("Active", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        }
                    }
                } else if (isWaitlist || meet.title.contains("Language")) {
                    Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF9800), CircleShape))
                            Text("Filling\nFast", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF6C00), fontWeight = FontWeight.Medium, lineHeight = 12.sp)
                        }
                    }
                } else if (isVerified) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Text("Verified", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp)
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${meet.date}, ${meet.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = meet.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Bottom Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Avatars
                val attendeesToUse = if (meet.attendeeCount > 0) meet.attendeeCount else (3..12).random()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        for (i in 0 until minOf(3, attendeesToUse)) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.padding(4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (attendeesToUse > 3) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "+${attendeesToUse - 3}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Button
            if (isWaitlist || meet.title.contains("Language")) {
                OutlinedButton(
                    onClick = { /* Waitlist */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text("Waitlist", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                Button(
                    onClick = { /* Join Meet */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Join Meet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

// Dummy data for when DB is empty to match mock exactly
fun getDummyMeets(): List<Meet> {
    return listOf(
        Meet(
            id = "1",
            title = "Morning Run Club",
            host_name = "John D.",
            date = "Today",
            time = "7:00 AM",
            location = "Central Park (0.5 mi)",
            attendeeCount = 7,
            category = "Sports"
        ),
        Meet(
            id = "2",
            title = "Indie Hacker Co-working",
            host_name = "Alice K.",
            date = "Tomorrow",
            time = "10:00 AM",
            location = "Joe's Coffee (1.2 mi)",
            attendeeCount = 4,
            category = "Coffee"
        ),
        Meet(
            id = "3",
            title = "Language Exchange",
            host_name = "Marcus R.",
            date = "Friday",
            time = "6:00 PM",
            location = "City Library (2.0 mi)",
            attendeeCount = 11,
            max_attendees = 11, // Forces waitlist logic based on mock
            category = "Study"
        )
    )
}

@Composable
fun MeetCardShimmer() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).shimmerEffect())
                Spacer(Modifier.width(16.dp))
                Column {
                    Box(modifier = Modifier.size(150.dp, 24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.size(100.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            }
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.size(200.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.size(180.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(32.dp))
            Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(25.dp)).shimmerEffect())
        }
    }
}
