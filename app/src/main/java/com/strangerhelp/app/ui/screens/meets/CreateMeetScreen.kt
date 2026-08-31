package com.strangerhelp.app.ui.screens.meets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.strangerhelp.app.ui.screens.meets.MeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMeetScreen(
    viewModel: MeetViewModel,
    navController: NavController
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cleanup") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-09-15") } // Default date
    var time by remember { mutableStateOf("09:00") } // Default time
    var maxAttendees by remember { mutableStateOf("50") }
    var visibility by remember { mutableStateOf("public") }
    var anonymous by remember { mutableStateOf(false) }

    val categories = listOf("Cleanup", "Sports", "Study", "Food", "Social", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Meet") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        TextButton(
                            onClick = {
                                viewModel.createMeet(
                                    title = title,
                                    description = description,
                                    category = category,
                                    location = location,
                                    date = date,
                                    time = time,
                                    visibility = visibility,
                                    maxAttendees = maxAttendees.toIntOrNull() ?: 50,
                                    anonymous = anonymous,
                                    voiceNote = null
                                )
                            },
                            enabled = title.isNotBlank() && category.isNotBlank() && date.isNotBlank() && time.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }
            item {
                Text("Category *", fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories.size) { i ->
                        FilterChip(
                            selected = category == categories[i],
                            onClick = { category = categories[i] },
                            label = { Text(categories[i]) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD) *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (HH:MM) *") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = maxAttendees,
                    onValueChange = { maxAttendees = it.filter { c -> c.isDigit() } },
                    label = { Text("Max Attendees") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🔒 Private Meet", fontSize = 14.sp)
                    Switch(checked = visibility == "private", onCheckedChange = { visibility = if (it) "private" else "public" })
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🕵️ Host Anonymously", fontSize = 14.sp)
                    Switch(checked = anonymous, onCheckedChange = { anonymous = it })
                }
            }
            if (error != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = com.strangerhelp.app.ui.screens.meets.Warning.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                        Text(text = error ?: "", fontSize = 13.sp, color = com.strangerhelp.app.ui.screens.meets.Warning, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            navController.popBackStack()
            viewModel.clearSuccess()
        }
    }
}
