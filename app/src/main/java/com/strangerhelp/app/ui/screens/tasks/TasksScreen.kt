package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strangerhelp.app.StrangerHelpApp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

val PrimaryDark = Color(0xFF171717)
val AccentOrange = Color(0xFFF5A623) // From Warning/Images
val CyanDeep = Color(0xFF29BC9B)





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = (context.applicationContext as StrangerHelpApp).database
    val viewModel: TasksViewModel = viewModel(factory = TasksViewModelFactory(database.searchHistoryDao()))
    
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("newest") }
    
    val categories = listOf("All", "Task", "Document Submission", "Photo Proof", "Parcel Pickup", "Queue Standing", "Verification", "Event / Group Work", "Other")
    
    var tasks by remember { mutableStateOf(emptyList<Task>()) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(true) }
    var offset by remember { mutableStateOf(0) }
    val limit = 20
    
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Location
    var locationState by remember { mutableStateOf("ask") } // "ask", "input", "active"
    var cityName by remember { mutableStateOf("Mumbai") }
    var sortExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        delay(350)
        debouncedQuery = searchQuery
    }

    fun fetchTasks(isLoadMore: Boolean = false) {
        if (!isLoadMore) {
            loading = true
            offset = 0
            hasMore = true
        } else {
            loadingMore = true
        }

        scope.launch {
            try {
                val cat = if (selectedCategory == "All") null else selectedCategory
                val q = debouncedQuery.takeIf { it.isNotBlank() }
                
                val res = ApiClient.api.getTasks(
                    category = cat,
                    limit = limit,
                    offset = offset,
                    search = q
                )
                
                if (res.isSuccessful) {
                    val newTasks = res.body() ?: emptyList()
                    if (isLoadMore) {
                        tasks = tasks + newTasks
                    } else {
                        tasks = newTasks
                    }
                    hasMore = newTasks.size == limit
                    offset += limit
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
                loadingMore = false
            }
        }
    }

    LaunchedEffect(debouncedQuery, selectedCategory, sortBy) {
        fetchTasks(isLoadMore = false)
    }

    // Infinite scroll
    LaunchedEffect(listState.layoutInfo.visibleItemsInfo.lastOrNull()) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
        if (lastVisible != null && lastVisible.index >= tasks.size - 3 && hasMore && !loadingMore && !loading) {
            fetchTasks(isLoadMore = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("post") },
                containerColor = AccentOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Search + Sort
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tasks...", color = androidx.compose.ui.graphics.Color(0xFF666666)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = androidx.compose.ui.graphics.Color(0xFF666666)) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDark,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Sort Dropdown
                Box {
                    IconButton(
                        onClick = { sortExpanded = true },
                        modifier = Modifier
                            .size(50.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = PrimaryDark)
                    }
                    DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        listOf("newest" to "Newest first", "distance" to "Nearest first", "budget_high" to "Budget: High to Low").forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { sortBy = key; sortExpanded = false }
                            )
                        }
                    }
                }
            }

            // Location Banner
            Surface(
                color = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF666666), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tasks near ", fontSize = 14.sp, color = androidx.compose.ui.graphics.Color(0xFF666666))
                        Text(cityName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
                    }
                    Text("Change", color = Color(0xFF007AFF), fontSize = 14.sp, modifier = Modifier.clickable { })
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        modifier = Modifier.clickable { selectedCategory = category },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) PrimaryDark else Color.Transparent,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else PrimaryDark,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Task List
            if (loading && tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentOrange)
                }
            } else if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("No tasks found", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryDark)
                        Text("Try adjusting your search", color = androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { navController.navigate("post") }, colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                            Text("Post a Task", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it._id }) { task ->
                        TaskCard(task = task, onClick = { navController.navigate("task/${task._id}") })
                    }
                    if (loadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AccentOrange, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier.animateContentSize().fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Category Chip
                    Surface(
                        color = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Description, null, modifier = Modifier.size(12.dp), tint = androidx.compose.ui.graphics.Color(0xFF666666))
                            Spacer(Modifier.width(4.dp))
                            Text(task.category, fontSize = 12.sp, color = PrimaryDark)
                        }
                    }
                    
                    if (task.urgent == 1) {
                        Surface(
                            color = androidx.compose.ui.graphics.Color(0xFFEE0000).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, null, modifier = Modifier.size(12.dp), tint = androidx.compose.ui.graphics.Color(0xFFEE0000))
                                Spacer(Modifier.width(2.dp))
                                Text("Urgent", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    
                    if (task.maxClaimers > 1) {
                        Surface(
                            color = Color(0xFF6C5CE7).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, null, modifier = Modifier.size(12.dp), tint = Color(0xFF6C5CE7))
                                Spacer(Modifier.width(2.dp))
                                Text("${task.maxClaimers}", fontSize = 12.sp, color = Color(0xFF6C5CE7), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                
                Text(
                    text = "₹${task.budget}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AccentOrange
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Title
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PrimaryDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = task.description.take(100) + if (task.description.length > 100) "..." else "",
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color(0xFF666666),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Location & Deadline
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(14.dp), tint = androidx.compose.ui.graphics.Color(0xFF666666))
                Spacer(Modifier.width(4.dp))
                Text(task.location.split(",").firstOrNull() ?: task.location, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF666666), maxLines = 1, modifier = Modifier.widthIn(max = 120.dp), overflow = TextOverflow.Ellipsis)
                
                Text("  •  ", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF666666))
                
                Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(14.dp), tint = androidx.compose.ui.graphics.Color(0xFF666666))
                Spacer(Modifier.width(4.dp))
                Text(task.deadline, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF666666))
            }
            
            // ⭐ Attachment Indicator
            if (task.attachmentCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = androidx.compose.ui.graphics.Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${task.attachmentCount} file${if (task.attachmentCount > 1) "s" else ""}",
                        fontSize = 11.sp,
                        color = androidx.compose.ui.graphics.Color(0xFF666666)
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)
            
            // Bottom Row: Poster + Posted Time + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Poster info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (task.anonymous == 1) "?" else (task.posterName.firstOrNull()?.uppercase() ?: "U"),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))

                    // Name
                    Text(
                        text = if (task.anonymous == 1) "Anonymous" else task.posterName.takeIf { it.isNotBlank() } ?: "User",
                        fontSize = 12.sp,
                        color = PrimaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Verified badge
                    if (task.posterVerified && task.anonymous == 0) {
                        Text(
                            text = " ✓",
                            fontSize = 10.sp,
                            color = androidx.compose.ui.graphics.Color(0xFF10B981)
                        )
                    }
                }

                // Right: Posted Time
                Text(
                    text = com.strangerhelp.app.utils.TimeUtils.getTimeAgo(task.createdAt),
                    fontSize = 10.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF666666),
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Status Badge
                val statusColor = when(task.status) {
                    "open" -> PrimaryDark
                    "claimed" -> AccentOrange
                    "completed" -> CyanDeep
                    else -> androidx.compose.ui.graphics.Color(0xFF666666)
                }
                Text(task.status.capitalize(), color = statusColor, fontSize = 12.sp)
            }
        }
    }
}
