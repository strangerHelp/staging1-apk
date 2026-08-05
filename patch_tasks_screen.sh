cat << 'INNER_EOF' > app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt
package com.strangerhelp.app.ui.screens.tasks

import android.Manifest
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
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
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.strangerhelp.app.ui.components.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Task", "Document Submission", "Photo Proof", "Parcel Pickup", "Queue Standing", "Verification", "Event / Group Work", "Other")

    var tasks by remember { mutableStateOf(emptyList<Task>()) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    
    var showFilters by remember { mutableStateOf(false) }
    var urgentFilter by remember { mutableStateOf(false) }
    
    var offset by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    val limit = 20
    
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()
    
    // Debounce search
    LaunchedEffect(searchQuery) {
        delay(500)
        debouncedQuery = searchQuery
    }
    
    fun fetchTasks(isLoadMore: Boolean = false) {
        if (!isLoadMore) {
            loading = true
            offset = 0
        } else {
            loadingMore = true
        }
        
        scope.launch {
            try {
                val cat = if (selectedCategory == "All") null else selectedCategory
                val q = debouncedQuery.takeIf { it.isNotBlank() }
                val urgentStr = if (urgentFilter) "1" else null
                
                val res = ApiClient.api.getTasks(
                    category = cat, 
                    limit = limit, 
                    offset = offset,
                    search = q,
                    urgent = urgentStr
                )
                
                if (res.isSuccessful) {
                    val body = res.body() ?: emptyList()
                    if (isLoadMore) {
                        tasks = tasks + body
                    } else {
                        tasks = body
                    }
                    hasMore = res.headers()["X-Has-More"] == "true" || body.size == limit
                    offset += limit
                }
            } catch (_: Exception) {}
            loading = false
            loadingMore = false
            pullRefreshState.endRefresh()
        }
    }
    
    LaunchedEffect(selectedCategory, debouncedQuery, urgentFilter) {
        fetchTasks(isLoadMore = false)
    }

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            fetchTasks(isLoadMore = false)
        }
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(pullRefreshState.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Tasks", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Text("Find tasks near you", color = Muted, style = MaterialTheme.typography.bodyMedium)
                
                Spacer(Modifier.height(16.dp))
                
                // Search & Filter Row
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).height(50.dp),
                        placeholder = { Text("Search tasks...") },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Muted) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, null, tint = Muted)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Hairline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    FilledIconButton(
                        onClick = { showFilters = true },
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (urgentFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Filled.FilterList, null)
                    }
                }
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
            
            if (loading && tasks.isEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(5) { TaskCardShimmer() }
                }
            } else if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No tasks found matching your filters.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tasks.size) { index ->
                        val task = tasks[index]
                        TaskCard(task = task, onClick = { navController.navigate("task/${task._id}") })
                        
                        // Load more trigger
                        if (index == tasks.size - 1 && hasMore && !loadingMore) {
                            LaunchedEffect(index) {
                                fetchTasks(isLoadMore = true)
                            }
                        }
                    }
                    if (loadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
        
        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Urgent Tasks Only", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = urgentFilter, onCheckedChange = { urgentFilter = it })
                }
                
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showFilters = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Apply Filters")
                }
                Spacer(Modifier.height(32.dp))
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
        HorizontalDivider(color = Hairline)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (task.anonymous == 1) "Anonymous" else (task.posterName.ifEmpty { task.posterId.take(8) }),
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

@Composable
fun TaskCardShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(8.dp))
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(60.dp, 20.dp).clip(RoundedCornerShape(50)).shimmerEffect())
            Box(modifier = Modifier.size(40.dp, 20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
    }
}
INNER_EOF
