package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.ui.components.EmptyState
import com.strangerhelp.app.ui.components.TaskCardSkeleton
import com.strangerhelp.app.ui.screens.feed.RecentTaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(
    filter: String = "all",
    navController: NavController,
    viewModel: MyTasksViewModel
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(filter) {
        viewModel.loadTasks(filter = filter)
    }

    val title = when (filter) {
        "posted" -> "Tasks I've Posted"
        "claimed" -> "Tasks I've Claimed"
        "completed" -> "Completed Tasks"
        else -> "My Tasks"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
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
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = when (filter) {
                        "posted" -> "📋"
                        "claimed" -> "📦"
                        "completed" -> "✅"
                        else -> "📭"
                    },
                    title = when (filter) {
                        "posted" -> "No tasks posted yet"
                        "claimed" -> "No tasks claimed yet"
                        "completed" -> "No completed tasks"
                        else -> "No tasks found"
                    },
                    message = when (filter) {
                        "posted" -> "Post your first task to get started"
                        "claimed" -> "Claim a task to help someone"
                        "completed" -> "Complete tasks to build your reputation"
                        else -> "Tasks you've interacted with"
                    }
                )
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
