package com.strangerhelp.app.ui.screens.post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostMeetScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("public") }
    var maxAttendees by remember { mutableStateOf("50") }
    var isAnonymous by remember { mutableStateOf(false) }

    val categories = listOf("Discussion", "Plantation", "Cleanup", "Meetup", "Workshop", "Sports", "Social", "Other")

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Host a Meet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Organize a community event", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Meet Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = category, onValueChange = {},
                label = { Text("Category") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = date, onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = time, onValueChange = { time = it },
                label = { Text("Time (HH:MM)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = location, onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            var visExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = visExpanded, onExpandedChange = { visExpanded = !visExpanded }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = visibility, onValueChange = {},
                    label = { Text("Visibility") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(visExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = visExpanded, onDismissRequest = { visExpanded = false }) {
                    DropdownMenuItem(text = { Text("Public") }, onClick = { visibility = "public"; visExpanded = false })
                    DropdownMenuItem(text = { Text("Private") }, onClick = { visibility = "private"; visExpanded = false })
                }
            }

            OutlinedTextField(
                value = maxAttendees, onValueChange = { maxAttendees = it.filter { c -> c.isDigit() } },
                label = { Text("Max Attendees") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(Icons.Filled.Mic, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Record Voice Note (Optional)")
        }
        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🕵️ Anonymous Posting", fontWeight = FontWeight.SemiBold)
                    Text("Hide your name on this meet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
            }
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Create Meet", fontWeight = FontWeight.SemiBold)
        }
    }
}
