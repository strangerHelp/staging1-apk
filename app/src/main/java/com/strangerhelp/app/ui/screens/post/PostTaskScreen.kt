package com.strangerhelp.app.ui.screens.post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.strangerhelp.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTaskScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("Today") }
    var isAnonymous by remember { mutableStateOf(false) }
    var maxClaimers by remember { mutableStateOf("2") }
    var isUrgent by remember { mutableStateOf(false) }
    var isPosting by remember { mutableStateOf(false) }

    val categories = listOf("Task", "Document Submission", "Photo Proof", "Parcel Pickup", "Queue Standing", "Verification", "Event / Group Work", "Other")
    val deadlines = listOf("Within 1 hour", "Today", "Tomorrow", "Custom")

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Post a Task", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Describe what you need done", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Task Title") },
            placeholder = { Text("e.g., Submit documents at RTO") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("Provide details...") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
        )
        Spacer(Modifier.height(12.dp))

        // Category dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = category, onValueChange = {},
                label = { Text("Category") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (category == "Event / Group Work") {
            OutlinedTextField(
                value = maxClaimers, onValueChange = { maxClaimers = it.filter { c -> c.isDigit() } },
                label = { Text("Number of Helpers needed") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
        }

        // Deadline dropdown
        var deadlineExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = deadlineExpanded, onExpandedChange = { deadlineExpanded = !deadlineExpanded }) {
            OutlinedTextField(
                value = deadline, onValueChange = {},
                label = { Text("Deadline") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(deadlineExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(expanded = deadlineExpanded, onDismissRequest = { deadlineExpanded = false }) {
                deadlines.forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { deadline = it; deadlineExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = budget, onValueChange = { budget = it.filter { c -> c.isDigit() } },
                label = { Text("Budget (₹)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
        }
        Spacer(Modifier.height(16.dp))

        // Urgent toggle
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("⚡ Urgent", fontWeight = FontWeight.SemiBold)
                    Text("Helpers will prioritize this", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isUrgent, onCheckedChange = { isUrgent = it })
            }
        }
        Spacer(Modifier.height(16.dp))
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🕵️ Anonymous Posting", fontWeight = FontWeight.SemiBold)
                    Text("Hide your name on this task", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
            }
        }
        Spacer(Modifier.height(16.dp))

        // Attachments
        OutlinedButton(
            onClick = { /* TODO: Implement file picker */ },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(androidx.compose.material.icons.Icons.Filled.AttachFile, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Attachments (Photos, Docs)")
        }
        Spacer(Modifier.height(12.dp))

        // Voice Note
        OutlinedButton(
            onClick = { /* TODO: Implement voice recording */ },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(androidx.compose.material.icons.Icons.Filled.Mic, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Record Voice Note (Optional)")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { isPosting = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            enabled = title.isNotBlank() && category.isNotBlank() && budget.isNotBlank() && location.isNotBlank() && !isPosting,
        ) {
            Text(if (isPosting) "Posting..." else "Post Task", fontWeight = FontWeight.SemiBold)
        }
    }
}
