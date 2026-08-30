package com.strangerhelp.app.ui.screens.post

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Info


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.strangerhelp.app.ui.components.LocationPicker

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
    var isPrivate by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    var selectedFileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedFileUris = uris
    }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(isPosting) {
        if (isPosting) {
            try {
                val mediaType = "text/plain".toMediaTypeOrNull()
                val t = title.toRequestBody(mediaType)
                val d = description.toRequestBody(mediaType)
                val c = category.toRequestBody(mediaType)
                val b = budget.toRequestBody(mediaType)
                val l = location.toRequestBody(mediaType)
                val u = (if (isUrgent) "1" else "0").toRequestBody(mediaType)
                val v = (if (isPrivate) "private" else "public").toRequestBody(mediaType)
                val dl = deadline.toRequestBody(mediaType)
                
                val filesParts = selectedFileUris.mapNotNull { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val requestFile = bytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                        okhttp3.MultipartBody.Part.createFormData("files", "attachment", requestFile)
                    } else {
                        null
                    }
                }
                
                val res = com.strangerhelp.app.data.api.ApiClient.api.postTask(
                    title = t,
                    description = d,
                    category = c,
                    budget = b,
                    location = l,
                    deadline = dl,
                    urgent = u,
                    visibility = v,
                    files = filesParts.ifEmpty { null }
                )
                
                if (res.isSuccessful) {
                    val inviteCode = res.body()?.get("inviteCode")
                    if (isPrivate && inviteCode != null) {
                        val sendIntent: android.content.Intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "Join my private task on StrangerHelp: https://strangerhelp.com/tasks/${res.body()?.get("taskId")}?invite=$inviteCode")
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                    navController.popBackStack()
                } else {
                    isPosting = false
                }
            } catch (e: Exception) {
                isPosting = false
            }
        }
    }
    
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
                    DropdownMenuItem(text = { Text(it) }, onClick = { 
                        if (it == "Custom") {
                            showDatePicker = true
                        } else {
                            deadline = it
                        }
                        deadlineExpanded = false 
                    })
                }
            }
        }
        
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    }) { Text("Next") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showTimePicker = false
                        val cal = Calendar.getInstance()
                        datePickerState.selectedDateMillis?.let { cal.timeInMillis = it }
                        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        cal.set(Calendar.MINUTE, timePickerState.minute)
                        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        deadline = format.format(cal.time)
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedTextField(
            value = budget, onValueChange = { budget = it.filter { c -> c.isDigit() } },
            label = { Text("Budget (₹)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            
            singleLine = true,
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text("Location", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        LocationPicker(onLocationSelected = { lat, lng, addr -> location = addr })
        if (location.isNotEmpty()) {
            Text("Selected: $location", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(Modifier.height(16.dp))
        
        val darkSwitchColors = SwitchDefaults.colors(
            uncheckedTrackColor = MaterialTheme.colorScheme.surface,
            uncheckedBorderColor = MaterialTheme.colorScheme.onSurface,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("⚡ Urgent", fontWeight = FontWeight.SemiBold)
                    Text("Helpers will prioritize this", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isUrgent, onCheckedChange = { isUrgent = it }, colors = darkSwitchColors)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🕵️ Anonymous Posting", fontWeight = FontWeight.SemiBold)
                    Text("Hide your name on this task", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it }, colors = darkSwitchColors)
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🔒 Private Task", fontWeight = FontWeight.SemiBold)
                    Text("Only visible via invite link", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isPrivate, onCheckedChange = { isPrivate = it }, colors = darkSwitchColors)
            }
        }
        
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { filePickerLauncher.launch("*/*") },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Filled.AttachFile, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedFileUris.isNotEmpty()) "${selectedFileUris.size} Attachment(s) Added" else "Add Attachments (Photos, Docs)")
        }
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = { /* TODO: Implement voice recording */ },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Filled.Mic, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Record Voice Note (Optional)")
        }
        
        Spacer(Modifier.height(24.dp))
        

        // ⭐ P2P Payment Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFFFF8F00), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("You will pay the helper directly via UPI after task completion. StrangerHelp does not hold or process payments.", fontSize = 12.sp, color = Color(0xFFE65100), lineHeight = 16.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
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
