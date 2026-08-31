package com.strangerhelp.app.ui.screens.post

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import com.strangerhelp.app.ui.components.LocationPicker

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start() {
        outputFile = File(context.cacheDir, "voice-${System.currentTimeMillis()}.webm")
        recorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.WEBM)
            setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(24000)
            setOutputFile(outputFile!!.absolutePath)
            prepare()
            start()
        }
    }

    fun stop(): ByteArray? {
        try {
            recorder?.apply { stop(); release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
        return outputFile?.readBytes()
    }
}

fun compressImage(context: Context, uri: Uri, maxDim: Int, quality: Int): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()
    if (bitmap == null) return ByteArray(0)

    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
    val width = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
    val height = if (ratio > 1) (maxDim / ratio).toInt() else maxDim

    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    val outputStream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return outputStream.toByteArray()
}

@Composable
fun VoiceNoteField(recorder: VoiceRecorder, onRecorded: (ByteArray?) -> Unit, disabled: Boolean = false) {
    var state by remember { mutableStateOf("idle") }
    var seconds by remember { mutableStateOf(0) }
    var bytes by remember { mutableStateOf<ByteArray?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            recorder.start()
            state = "recording"
            seconds = 0
        }
    }

    when (state) {
        "idle" -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add a voice description for helpers", modifier = Modifier.weight(1f), fontSize = 14.sp)
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                enabled = !disabled,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Record")
            }
        }
        "recording" -> {
            LaunchedEffect(Unit) {
                while (state == "recording") {
                    delay(1000)
                    seconds++
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔴 ${seconds/60}:${(seconds%60).toString().padStart(2, '0')}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Button(onClick = { 
                    bytes = recorder.stop()
                    onRecorded(bytes)
                    state = "done" 
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Stop")
                }
            }
        }
        "done" -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✅ Voice note attached", color = Color(0xFF00BFA5), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = { 
                bytes = null
                onRecorded(null)
                state = "idle" 
            }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTaskScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var taskLat by remember { mutableStateOf("") }
    var taskLng by remember { mutableStateOf("") }
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
    
    var selectedFileBytes by remember { mutableStateOf<List<ByteArray>>(emptyList()) }
    var voiceNoteBytes by remember { mutableStateOf<ByteArray?>(null) }
    
    val voiceRecorder = remember { VoiceRecorder(context) }
    
    val pickFiles = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val maxAllowed = if (voiceNoteBytes != null) 4 else 5
        val toProcess = uris.take(maxAllowed)
        // Compress images
        val bytesList = toProcess.mapNotNull { uri ->
            try {
                compressImage(context, uri, 1200, 75)
            } catch (e: Exception) {
                null
            }
        }.filter { it.isNotEmpty() }
        selectedFileBytes = bytesList
    }

    LaunchedEffect(isPosting) {
        if (isPosting) {
            try {
                val mediaType = "text/plain".toMediaTypeOrNull()
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("title", title)
                    .addFormDataPart("description", description)
                    .addFormDataPart("category", category)
                    .addFormDataPart("budget", budget)
                    .addFormDataPart("deadline", deadline)
                    .addFormDataPart("location", location)
                    .addFormDataPart("anonymous", if (isAnonymous) "true" else "false")
                    .addFormDataPart("urgent", if (isUrgent) "true" else "false")
                    .addFormDataPart("visibility", if (isPrivate) "private" else "public")

                if (category == "Event / Group Work") {
                    builder.addFormDataPart("max_claimers", maxClaimers)
                }
                
                // Location coords (mocked for now since LocationPicker doesn't provide lat/lng in this template, just address)
                // In a real app we'd get this from the picker
                if (taskLat.isNotEmpty() && taskLat != "0.0") builder.addFormDataPart("lat", taskLat)
                if (taskLng.isNotEmpty() && taskLng != "0.0") builder.addFormDataPart("lng", taskLng)

                selectedFileBytes.forEachIndexed { i, bytes ->
                    builder.addFormDataPart(
                        "files", "attachment_$i.jpg",
                        bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }

                voiceNoteBytes?.let {
                    builder.addFormDataPart(
                        "files", "voice-note.webm",
                        it.toRequestBody("audio/webm".toMediaTypeOrNull())
                    )
                }

                val res = com.strangerhelp.app.data.api.ApiClient.api.postTask(builder.build())

                if (res.isSuccessful) {
                    val inviteCode = res.body()?.get("inviteCode")
                    if (isPrivate && inviteCode != null) {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "Join my private task on StrangerHelp: https://strangerhelp.com/tasks/${res.body()?.get("id")}?invite=$inviteCode")
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                    }
                    navController.popBackStack()
                } else {
                    android.util.Log.e("PostTaskError", "Error: ${res.errorBody()?.string()} - ${res.code()} - ${res.message()}")
                    android.widget.Toast.makeText(context, "Failed to post task", android.widget.Toast.LENGTH_SHORT).show()
                    isPosting = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("PostTaskError", "Exception: ${e.message}")
                    android.widget.Toast.makeText(context, "Failed to post task", android.widget.Toast.LENGTH_SHORT).show()
                    isPosting = false
            }
        }
    }
    
    val categories = listOf("Task", "Document Submission", "Photo Proof", "Parcel Pickup", "Queue Standing", "Verification", "Event / Group Work", "Other")
    val deadlines = listOf("Within 1 hour", "Within 2 hours", "Within 4 hours", "Today", "Tomorrow", "Custom")

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
                shape = RoundedCornerShape(12.dp)
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
                label = { Text("How many helpers?") },
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
                shape = RoundedCornerShape(12.dp)
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
                        
                        // Exact format requested in the guide
                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
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
            label = { Text(if (category == "Event / Group Work") "Budget (₹) / per person" else "Budget (₹)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text("Location", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        LocationPicker(onLocationSelected = { lat, lng, addr -> location = addr; taskLat = lat.toString(); taskLng = lng.toString() })
        if (location.isNotEmpty()) {
            Text("Selected: $location", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text("Attachments & Voice Note", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                VoiceNoteField(
                    recorder = voiceRecorder, 
                    onRecorded = { voiceNoteBytes = it },
                    disabled = selectedFileBytes.size >= 5
                )
                
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                
                Button(
                    onClick = { pickFiles.launch("image/*") },
                    enabled = selectedFileBytes.size + (if (voiceNoteBytes != null) 1 else 0) < 5,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Photos (${selectedFileBytes.size}/5 max)")
                }
                
                if (selectedFileBytes.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedFileBytes.forEachIndexed { idx, _ ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text("Photo ${idx+1}") },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close, 
                                        null, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }
            }
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
        
        Spacer(Modifier.height(12.dp))
        
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

        Spacer(Modifier.height(12.dp))

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
