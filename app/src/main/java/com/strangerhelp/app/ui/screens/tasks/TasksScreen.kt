package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.testTag
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.repository.TaskRepository
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
fun TasksScreen(
    navController: NavController,
    initialCategory: String? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = (context.applicationContext as StrangerHelpApp).database
    val viewModel: TasksViewModel = viewModel(
        factory = TasksViewModelFactory(
            dao = database.searchHistoryDao(),
            repository = TaskRepository(ApiClient.api, database.taskDao())
        )
    )
    val focusManager = LocalFocusManager.current
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val isFromCache by viewModel.isFromCache.collectAsStateWithLifecycle()
    val hasMore by viewModel.hasMore.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var showHistoryDropdown by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: "All") }
    var sortBy by remember { mutableStateOf("newest") }
    
    val categories = listOf("All", "Task", "Document Submission", "Photo Proof", "Parcel Pickup", "Queue Standing", "Verification", "Event / Group Work", "Other")
    
    val listState = rememberLazyListState()

    // Location
    var locationState by remember { mutableStateOf("ask") } // "ask", "input", "active"
    var cityName by remember { mutableStateOf("Mumbai") }
    var sortExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        delay(350)
        debouncedQuery = searchQuery
    }

    LaunchedEffect(debouncedQuery) {
        if (debouncedQuery.trim().length >= 2) {
            viewModel.saveSearch(debouncedQuery.trim())
        }
    }

    LaunchedEffect(debouncedQuery, selectedCategory, sortBy) {
        viewModel.fetchTasks(
            category = selectedCategory,
            query = debouncedQuery.takeIf { it.isNotBlank() },
            sortBy = sortBy,
            isLoadMore = false
        )
    }

    // Infinite scroll
    LaunchedEffect(listState.layoutInfo.visibleItemsInfo.lastOrNull()) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
        if (lastVisible != null && lastVisible.index >= tasks.size - 3 && hasMore && !loadingMore && !loading && !isOffline) {
            viewModel.fetchTasks(
                category = selectedCategory,
                query = debouncedQuery.takeIf { it.isNotBlank() },
                sortBy = sortBy,
                isLoadMore = true
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                .imePadding()
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
                    onValueChange = { 
                        searchQuery = it 
                        if (!showHistoryDropdown && recentSearches.isNotEmpty()) {
                            showHistoryDropdown = true
                        }
                    },
                    placeholder = { Text("Search tasks...", color = androidx.compose.ui.graphics.Color(0xFF666666)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = androidx.compose.ui.graphics.Color(0xFF666666)) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { 
                                        searchQuery = "" 
                                        debouncedQuery = ""
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = Color(0xFF666666),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (recentSearches.isNotEmpty()) {
                                IconButton(
                                    onClick = { showHistoryDropdown = !showHistoryDropdown }
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "Search history",
                                        tint = if (showHistoryDropdown) AccentOrange else Color(0xFF666666),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .onFocusChanged {
                            isSearchFocused = it.isFocused
                            if (it.isFocused && recentSearches.isNotEmpty()) {
                                showHistoryDropdown = true
                            }
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.saveSearch(searchQuery)
                            }
                            showHistoryDropdown = false
                            focusManager.clearFocus()
                        }
                    ),
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

            // Search History Dropdown Card (visible when search is focused/active with history)
            if (showHistoryDropdown && recentSearches.isNotEmpty()) {
                val displayHistory = if (searchQuery.isBlank()) recentSearches else recentSearches.filter {
                    it.query.contains(searchQuery, ignoreCase = true)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E5E5))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Recent Searches",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        viewModel.clearAllSearches()
                                        showHistoryDropdown = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("Clear All", fontSize = 12.sp, color = Color(0xFF888888))
                                }
                                IconButton(
                                    onClick = { showHistoryDropdown = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close history",
                                        tint = Color(0xFF888888),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(4.dp))

                        if (displayHistory.isEmpty()) {
                            Text(
                                "No matching previous searches",
                                fontSize = 12.sp,
                                color = Color(0xFF999999),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        } else {
                            displayHistory.forEach { historyItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = historyItem.query
                                            debouncedQuery = historyItem.query
                                            viewModel.saveSearch(historyItem.query)
                                            showHistoryDropdown = false
                                            focusManager.clearFocus()
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.History,
                                        contentDescription = null,
                                        tint = Color(0xFF757575),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = historyItem.query,
                                        fontSize = 14.sp,
                                        color = PrimaryDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteSearch(historyItem.query) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove query",
                                            tint = Color(0xFF999999),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (recentSearches.isNotEmpty() && !showHistoryDropdown) {
                // Quick Re-access Horizontal Chip Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "Recent Searches",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Recent:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666)
                    )
                    Spacer(Modifier.width(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        items(recentSearches) { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF3F4F6),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                modifier = Modifier.clickable {
                                    searchQuery = item.query
                                    debouncedQuery = item.query
                                    viewModel.saveSearch(item.query)
                                    focusManager.clearFocus()
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = item.query,
                                        fontSize = 11.sp,
                                        color = PrimaryDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete search",
                                        tint = Color(0xFF888888),
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable {
                                                viewModel.deleteSearch(item.query)
                                            }
                                    )
                                }
                            }
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

            // Subtle UI Indicator: Offline & Room Local Cache Banner
            AnimatedVisibility(
                visible = isOffline && tasks.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("offline_cached_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFFEF3C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOffline) Icons.Outlined.CloudOff else Icons.Outlined.Storage,
                                    contentDescription = "Room Database Cache",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isOffline) "Offline Mode" else "Cached Data",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFDE68A)
                                    ) {
                                        Text(
                                            text = "ROOM DB",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF78350F),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isOffline) {
                                        "Viewing ${tasks.size} cached ${if (tasks.size == 1) "task" else "tasks"} from local Room database"
                                    } else {
                                        "Viewing cached tasks from local Room database"
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309).copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Sync / Retry button
                        TextButton(
                            onClick = {
                                viewModel.fetchTasks(
                                    category = selectedCategory,
                                    query = debouncedQuery.takeIf { it.isNotBlank() },
                                    sortBy = sortBy,
                                    isLoadMore = false
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color(0xFFFEF3C7),
                                contentColor = Color(0xFF92400E)
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("offline_retry_button")
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Retry connection",
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(if (isOffline) "⚡" else "📭", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (isOffline) "No cached tasks available" else "No tasks found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryDark
                        )
                        Text(
                            text = if (isOffline) "Connect to the internet to load and cache tasks for offline viewing." else "Try adjusting your search or category",
                            color = androidx.compose.ui.graphics.Color(0xFF666666),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        if (isOffline) {
                            Button(
                                onClick = {
                                    viewModel.fetchTasks(
                                        category = selectedCategory,
                                        query = debouncedQuery.takeIf { it.isNotBlank() },
                                        sortBy = sortBy,
                                        isLoadMore = false
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Retry Connection", color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = { navController.navigate("post") },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                            ) {
                                Text("Post a Task", color = Color.White)
                            }
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
                        TaskCard(
                            task = task,
                            isOffline = isOffline,
                            onClick = { navController.navigate("task/${task._id}") }
                        )
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
fun TaskCard(task: Task, isOffline: Boolean = false, onClick: () -> Unit) {
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

                    if (isOffline) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Storage, contentDescription = "Room Cache", modifier = Modifier.size(11.dp), tint = Color(0xFFB45309))
                                Spacer(Modifier.width(3.dp))
                                Text("Room Cache", fontSize = 11.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Medium)
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
            val titleText = task.title.orEmpty()
            Text(
                text = titleText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PrimaryDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(4.dp))
            
            val descText = task.description.orEmpty()
            Text(
                text = descText.take(100) + if (descText.length > 100) "..." else "",
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color(0xFF666666),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Location & Deadline
            val locText = task.location.orEmpty().split(",").firstOrNull()?.trim().orEmpty().ifBlank { "Location" }
            val deadlineText = task.deadline.orEmpty().ifBlank { "Today" }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(14.dp), tint = androidx.compose.ui.graphics.Color(0xFF666666))
                Spacer(Modifier.width(4.dp))
                Text(locText, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF666666), maxLines = 1, modifier = Modifier.widthIn(max = 120.dp), overflow = TextOverflow.Ellipsis)
                
                Text("  •  ", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF666666))
                
                Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(14.dp), tint = androidx.compose.ui.graphics.Color(0xFF666666))
                Spacer(Modifier.width(4.dp))
                Text(deadlineText, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF666666))
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
                    val posterNameSafe = task.posterName.orEmpty().trim().ifBlank { "User" }
                    val avatarChar = if (task.anonymous == 1) "?" else (posterNameSafe.firstOrNull()?.uppercase() ?: "U")
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarChar,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))

                    // Name
                    val displayName = if (task.anonymous == 1) "Anonymous" else posterNameSafe
                    Text(
                        text = displayName,
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
                    text = com.strangerhelp.app.utils.TimeUtils.getTimeAgo(task.createdAt.orEmpty()),
                    fontSize = 10.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF666666),
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Status Badge
                val statusSafe = task.status.orEmpty().ifBlank { "open" }
                val statusColor = when(statusSafe.lowercase()) {
                    "open" -> PrimaryDark
                    "claimed" -> AccentOrange
                    "completed" -> CyanDeep
                    else -> androidx.compose.ui.graphics.Color(0xFF666666)
                }
                val statusFormatted = statusSafe.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }
                Text(statusFormatted, color = statusColor, fontSize = 12.sp)
            }
        }
    }
}
