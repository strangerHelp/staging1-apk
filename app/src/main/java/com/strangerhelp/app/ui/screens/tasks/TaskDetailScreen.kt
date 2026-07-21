package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(navController: NavController, user: User, taskId: String) {
    var task by remember { mutableStateOf<Task?>(null) }
    var loading by remember { mutableStateOf(true) }
    var claiming by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(taskId) {
        try {
            val res = ApiClient.api.getTask(taskId)
            if (res.isSuccessful) task = res.body()
        } catch (_: Exception) {}
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Detail") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (task == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Task not found", color = Muted) }
        } else {
            val t = task!!
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
                // Status + Category
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(t.category) }, shape = RoundedCornerShape(8.dp))
                    AssistChip(onClick = {}, label = { Text(t.status.replaceFirstChar { it.uppercase() }) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = when(t.status) { "open" -> CyanDeep.copy(0.1f); "claimed" -> Warning.copy(0.1f); else -> Muted.copy(0.1f) }),
                        shape = RoundedCornerShape(8.dp))
                    if (t.urgent == 1) AssistChip(onClick = {}, label = { Text("⚡ Urgent") }, colors = AssistChipDefaults.assistChipColors(containerColor = Error.copy(0.1f)), shape = RoundedCornerShape(8.dp))
                }

                Spacer(Modifier.height(16.dp))
                Text(t.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                if (t.description.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(t.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(24.dp))

                // Info grid
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard("Budget", "₹${t.budget}${if(t.maxClaimers>1) "/person" else ""}", Modifier.weight(1f))
                    InfoCard("Deadline", t.deadline, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard("Location", t.location, Modifier.weight(1f))
                    InfoCard("Posted by", if(t.anonymous==1) "Anonymous" else t.posterName, Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))

                // Actions
                if (t.status == "open" && t.posterId != user.id) {
                    Button(
                        onClick = {
                            claiming = true
                            scope.launch {
                                try { ApiClient.api.claimTask(taskId, mapOf("action" to "claim")) } catch (_: Exception) {}
                                // Refresh
                                try { val r = ApiClient.api.getTask(taskId); if(r.isSuccessful) task = r.body() } catch (_: Exception) {}
                                claiming = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        enabled = !claiming,
                    ) {
                        if (claiming) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text("Claim This Task", fontWeight = FontWeight.SemiBold)
                    }
                } else if (t.status == "claimed" && t.claimedBy == user.id) {
                    Text("✓ You claimed this task", color = CyanDeep, fontWeight = FontWeight.Medium)
                } else if (t.status == "claimed") {
                    Text("✓ Claimed by ${t.claimedByName ?: "a helper"}", color = Muted)
                } else if (t.status == "completed") {
                    Text("✓ Task completed", color = Muted)
                }
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}
