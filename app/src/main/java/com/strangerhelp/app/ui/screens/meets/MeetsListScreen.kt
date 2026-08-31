package com.strangerhelp.app.ui.screens.meets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.ui.screens.meets.MeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetsListScreen(
    viewModel: MeetViewModel,
    navController: NavController
) {
    val meets by viewModel.meets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var inviteCode by remember { mutableStateOf("") }

    val categories = listOf("All", "Cleanup", "Sports", "Study", "Food", "Social", "Other")

    LaunchedEffect(Unit) {
        viewModel.loadMeets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stranger Meets") },
                actions = {
                    IconButton(onClick = { navController.navigate("create_meet") }) {
                        Icon(Icons.Default.Add, "Create Meet")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    placeholder = { Text("Enter invite code") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (inviteCode.isNotBlank()) {
                            viewModel.joinByCode(inviteCode) { meetId ->
                                if (meetId != null) {
                                    navController.navigate("meet_detail/$meetId")
                                }
                            }
                        }
                    }
                ) {
                    Text("Join")
                }
            }

            if (isLoading && meets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (meets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 48.sp)
                        Text("No meets yet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Be the first to create one!", fontSize = 13.sp, color = Muted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(
                        meets.filter { selectedCategory == "All" || it.category == selectedCategory },
                        key = { it.id }
                    ) { meet ->
                        MeetCard(
                            meet = meet,
                            onClick = { navController.navigate("meet_detail/${meet.id}") }
                        )
                    }
                }
            }
        }
    }
}
