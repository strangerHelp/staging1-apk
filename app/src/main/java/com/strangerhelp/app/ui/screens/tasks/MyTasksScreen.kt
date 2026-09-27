package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.strangerhelp.app.ui.components.EmptyState
import com.strangerhelp.app.ui.components.TaskCardSkeleton
import com.strangerhelp.app.ui.screens.feed.RecentTaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(
    initialFilter: String = "all",
    navController: NavController,
    viewModel: MyTasksViewModel,
    currentUserId: String? = null
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    
    val postedCount by viewModel.postedCount.collectAsStateWithLifecycle()
    val claimedCount by viewModel.claimedCount.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedCount.collectAsStateWithLifecycle()
    val allCount = postedCount + claimedCount

    LaunchedEffect(initialFilter, currentUserId) {
        viewModel.loadTasks(filter = initialFilter, userId = currentUserId)
    }

    val tabs = listOf(
        Triple("all", "All", allCount),
        Triple("posted", "Posted", postedCount),
        Triple("claimed", "Claimed", claimedCount),
        Triple("completed", "Completed", completedCount)
    )

    val selectedTabIndex = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0)

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Tasks",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )

                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = Color(0xFFF57C00),
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                            color = Color(0xFFF57C00),
                            width = 32.dp
                        )
                    },
                    divider = { HorizontalDivider(color = Color(0xFFEEEEEE)) }
                ) {
                    tabs.forEachIndexed { index, (key, label, count) ->
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.setFilter(key) },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color(0xFFF57C00) else Color(0xFF64748B)
                                    )
                                    if (count > 0) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = if (isSelected) Color(0xFFFFECE0) else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) Color(0xFFF57C00) else Color(0xFF64748B),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading && tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                repeat(4) { TaskCardSkeleton() }
            }
        } else if (!isLoading && tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyState(
                        icon = when (activeTab) {
                            "posted" -> "📋"
                            "claimed" -> "📦"
                            "completed" -> "✅"
                            else -> "📭"
                        },
                        title = when (activeTab) {
                            "posted" -> "No tasks posted yet"
                            "claimed" -> "No tasks claimed yet"
                            "completed" -> "No completed tasks"
                            else -> "No tasks found"
                        },
                        message = when (activeTab) {
                            "posted" -> "Post a task to get prompt help from verified locals in your area."
                            "claimed" -> "You haven't claimed any tasks as a helper yet. Browse available tasks nearby to help and earn!"
                            "completed" -> "Tasks you complete as a helper or that helpers complete for you will appear here."
                            else -> "Tasks you interact with will appear here."
                        }
                    )

                    Spacer(Modifier.height(16.dp))
                    when (activeTab) {
                        "posted" -> {
                            Button(
                                onClick = { navController.navigate("post") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Post a Task", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        "claimed", "completed" -> {
                            Button(
                                onClick = { navController.navigate("tasks") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Explore Available Tasks", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(tasks, key = { it._id }) { task ->
                    RecentTaskCard(
                        task = task,
                        onClick = {
                            navController.navigate("task_detail/${task._id}")
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
