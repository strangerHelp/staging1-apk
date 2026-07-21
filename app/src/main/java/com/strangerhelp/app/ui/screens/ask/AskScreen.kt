package com.strangerhelp.app.ui.screens.ask

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.strangerhelp.app.ui.theme.*

data class Question(val title: String, val category: String, val location: String, val votes: Int, val timeAgo: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Tech Support", "Local Info", "Recommendations", "General")
    
    val questions = listOf(
        Question("Where can I find a good electrician in Andheri West?", "Local Info", "Mumbai", 12, "2h ago"),
        Question("My laptop screen is flickering. Is it a hardware issue?", "Tech Support", "Delhi", 8, "4h ago"),
        Question("Best place for authentic South Indian thali in Pune?", "Recommendations", "Pune", 24, "1d ago"),
        Question("How do I renew my driving license online?", "General", "Bangalore", 45, "2d ago")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask & Answer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("postQuestion") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Ask Question")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
            
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(questions.filter { selectedCategory == "All" || it.category == selectedCategory }) { q ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Icon(Icons.Filled.ArrowDropUp, "Upvote", tint = Muted)
                                Text("${q.votes}", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = q.category,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(q.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(q.location, style = MaterialTheme.typography.bodySmall, color = Muted)
                                    Text(" • ", style = MaterialTheme.typography.bodySmall, color = Muted)
                                    Text(q.timeAgo, style = MaterialTheme.typography.bodySmall, color = Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
