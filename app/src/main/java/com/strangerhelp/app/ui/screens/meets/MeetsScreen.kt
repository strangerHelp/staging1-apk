package com.strangerhelp.app.ui.screens.meets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Meet
import com.strangerhelp.app.ui.theme.CyanDeep
import com.strangerhelp.app.ui.theme.Muted
import kotlinx.coroutines.launch
import com.strangerhelp.app.ui.components.shimmerEffect
import androidx.compose.ui.draw.clip
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.strangerhelp.app.StrangerHelpApp

@Composable
fun MeetCardShimmer() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(60.dp, 24.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                Box(Modifier.size(50.dp, 20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.6f).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(80.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Box(Modifier.size(40.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.size(100.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetsScreen(navController: NavController) {
    val db = StrangerHelpApp.instance.database
    val meets by db.meetDao().getAllMeets().collectAsStateWithLifecycle(initialValue = emptyList())
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }
    var joinCode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val categories = listOf("All", "Hangout", "Tech", "Support", "Gaming", "Other")

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

    val filteredMeets = if (selectedCategory == "All") meets else meets.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Meets", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("postMeet") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("Host a Meet")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().nestedScroll(pullRefreshState.nestedScrollConnection)) {
            Column(Modifier.padding(16.dp)) {
                // Join via code
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { joinCode = it },
                        placeholder = { Text("Got an invite code?", color = Muted) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { /* Join */ }, shape = RoundedCornerShape(12.dp)) {
                        Text("Join")
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Categories
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (isLoading && meets.isEmpty()) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(3) { MeetCardShimmer() }
                    }
                } else if (filteredMeets.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No meets nearby", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(filteredMeets) { meet ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                                            Text(meet.category ?: "Meet", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                        }
                                        if (meet.visibility == "public") {
                                            Text("🌐 Public", color = CyanDeep, style = MaterialTheme.typography.labelSmall)
                                        } else {
                                            Text("🔒 Private", color = Muted, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(meet.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(meet.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("📍 ${meet.location}", style = MaterialTheme.typography.labelMedium)
                                        Text("👥 ${meet.attendeeCount} / ${meet.max_attendees}", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("🕒 ${meet.date} at ${meet.time}", style = MaterialTheme.typography.labelMedium, color = Muted)
                                    
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider()
                                    Spacer(Modifier.height(12.dp))
                                    
                                    Button(onClick = {
                                        scope.launch {
                                            try {
                                                ApiClient.api.actionMeet(meet.id, mapOf("action" to "join"))
                                            } catch (e: Exception) {}
                                        }
                                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                        Text("Join Meet")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
            )
        }
    }
}
