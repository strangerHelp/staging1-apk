package com.strangerhelp.app.ui.screens.post

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.strangerhelp.app.ui.components.LocationPicker
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start() {
        try {
            val file = File(context.cacheDir, "voice-${System.currentTimeMillis()}.webm")
            outputFile = file
            recorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.WEBM)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(24000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            recorder = null
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
fun VoiceNoteField(
    recorder: VoiceRecorder,
    hasVoiceNote: Boolean,
    onRecorded: (ByteArray?) -> Unit,
    disabled: Boolean = false
) {
    var state by remember(hasVoiceNote) {
        mutableStateOf(if (hasVoiceNote) "done" else "idle")
    }
    var seconds by remember { mutableStateOf(0) }

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
                Text(
                    "🔴 ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val bytes = recorder.stop()
                        onRecorded(bytes)
                        state = if (bytes != null) "done" else "idle"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Stop")
                }
            }
        }
        "done" -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "✅ Voice note attached",
                color = Color(0xFF00BFA5),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = {
                onRecorded(null)
                state = "idle"
            }) {
                Text("Remove", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTaskScreen(
    navController: NavController,
    viewModel: PostTaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDiscardDraftDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    val voiceRecorder = remember { VoiceRecorder(context) }

    // Intercept back navigation: Save draft automatically if user leaves/presses back
    BackHandler {
        viewModel.handleBack {
            navController.popBackStack()
        }
    }

    // Auto-save draft on dispose (e.g. if switching bottom navigation tabs)
    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveDraft(showToast = false)
        }
    }

    val pickFiles = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val maxAllowed = if (uiState.voiceNoteBytes != null) 4 else 5
        val toProcess = uris.take(maxAllowed)
        val bytesList = toProcess.mapNotNull { uri ->
            try {
                compressImage(context, uri, 1200, 75)
            } catch (e: Exception) {
                null
            }
        }.filter { it.isNotEmpty() }
        viewModel.setSelectedFiles(bytesList)
    }

    val categories = listOf(
        "Task",
        "Document Submission",
        "Photo Proof",
        "Parcel Pickup",
        "Queue Standing",
        "Verification",
        "Event / Group Work",
        "Other"
    )
    val deadlines = listOf("Within 1 hour", "Within 2 hours", "Within 4 hours", "Today", "Tomorrow", "Custom")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header Row with Back Button, Title, and Save Draft Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.handleBack {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.testTag("post_task_back_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    "Post a Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Describe what you need done",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.hasContent()) {
                TextButton(
                    onClick = { viewModel.saveDraft(showToast = true) },
                    modifier = Modifier.testTag("save_draft_button")
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = "Save Draft",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Save Draft", fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Restored Draft Banner
        if (uiState.isDraftRestored) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("draft_restored_banner"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = "Draft restored",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Resumed from Draft",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Your unsaved changes were saved. Continue where you stopped.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    TextButton(
                        onClick = { showDiscardDraftDialog = true },
                        modifier = Modifier.testTag("discard_draft_button")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Discard Draft",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "Discard",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Discard Confirmation Dialog
        if (showDiscardDraftDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDraftDialog = false },
                title = { Text("Discard Task Draft?") },
                text = { Text("Are you sure you want to discard this draft? All filled information and attachments will be cleared.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDiscardDraftDialog = false
                            viewModel.discardDraft()
                        }
                    ) {
                        Text("Discard", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDraftDialog = false }) {
                        Text("Keep Editing")
                    }
                }
            )
        }

        OutlinedTextField(
            value = uiState.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Task Title") },
            placeholder = { Text("e.g., Submit documents at RTO") },
            modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text("Description") },
            placeholder = { Text("Provide details...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("task_description_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = uiState.category,
                onValueChange = {},
                label = { Text("Category") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("task_category_dropdown"),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            viewModel.updateCategory(cat)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (uiState.category == "Event / Group Work") {
            OutlinedTextField(
                value = uiState.maxClaimers,
                onValueChange = { viewModel.updateMaxClaimers(it) },
                label = { Text("How many helpers?") },
                modifier = Modifier.fillMaxWidth().testTag("max_claimers_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
        }

        var deadlineExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = deadlineExpanded,
            onExpandedChange = { deadlineExpanded = !deadlineExpanded }
        ) {
            OutlinedTextField(
                value = uiState.deadline,
                onValueChange = {},
                label = { Text("Deadline") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(deadlineExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("deadline_dropdown"),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = deadlineExpanded,
                onDismissRequest = { deadlineExpanded = false }
            ) {
                deadlines.forEach { dl ->
                    DropdownMenuItem(
                        text = { Text(dl) },
                        onClick = {
                            if (dl == "Custom") {
                                showDatePicker = true
                            } else {
                                viewModel.updateDeadline(dl)
                            }
                            deadlineExpanded = false
                        }
                    )
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

                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        viewModel.updateDeadline(format.format(cal.time))
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
            value = uiState.budget,
            onValueChange = { viewModel.updateBudget(it) },
            label = {
                Text(
                    if (uiState.category == "Event / Group Work") "Budget (₹) / per person" else "Budget (₹)"
                )
            },
            modifier = Modifier.fillMaxWidth().testTag("budget_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        Spacer(Modifier.height(12.dp))

        Text("Location", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        LocationPicker(
            onLocationSelected = { lat, lng, addr ->
                viewModel.updateLocation(addr = addr, lat = lat, lng = lng)
            }
        )
        if (uiState.location.isNotEmpty()) {
            Text(
                "Selected: ${uiState.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("Attachments & Voice Note", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                VoiceNoteField(
                    recorder = voiceRecorder,
                    hasVoiceNote = uiState.voiceNoteBytes != null,
                    onRecorded = { viewModel.setVoiceNote(it) },
                    disabled = uiState.selectedFileBytes.size >= 5
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { pickFiles.launch("image/*") },
                    enabled = uiState.selectedFileBytes.size + (if (uiState.voiceNoteBytes != null) 1 else 0) < 5,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth().testTag("add_photos_button")
                ) {
                    Text("Add Photos (${uiState.selectedFileBytes.size}/5 max)")
                }

                if (uiState.selectedFileBytes.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.selectedFileBytes.forEachIndexed { idx, _ ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text("Photo ${idx + 1}") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { viewModel.removePhoto(idx) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove photo",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
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
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isUrgent) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = if (uiState.isUrgent) BorderStroke(1.dp, Color(0xFFFCA5A5)) else null
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Task Urgency & Priority", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Select urgency level so helpers can prioritize your task",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isUrgent,
                        onCheckedChange = { viewModel.updateUrgent(it) },
                        colors = darkSwitchColors
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Low", "Medium", "High").forEach { level ->
                        val isSelected = uiState.priority.equals(level, ignoreCase = true)
                        val chipColor = when (level) {
                            "High" -> Color(0xFFDC2626)
                            "Medium" -> Color(0xFFD97706)
                            else -> Color(0xFF059669)
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updatePriority(level) }
                                .testTag("priority_selector_${level.lowercase()}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) chipColor else Color.White.copy(alpha = 0.8f),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) {
                            Text(
                                text = if (level == "High") "⚡ High" else level,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF374151)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("🕵️ Anonymous Posting", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Hide your name on this task",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isAnonymous,
                    onCheckedChange = { viewModel.updateAnonymous(it) },
                    colors = darkSwitchColors
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("🔒 Private Task", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Only visible via invite link",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isPrivate,
                    onCheckedChange = { viewModel.updatePrivate(it) },
                    colors = darkSwitchColors
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // P2P Payment Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFFFF8F00),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "You will pay the helper directly via UPI after task completion. StrangerHelp does not hold or process payments.",
                    fontSize = 12.sp,
                    color = Color(0xFFE65100),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.submitTask(
                    context = context,
                    onSuccess = { inviteCode, taskId ->
                        if (uiState.isPrivate && inviteCode != null && taskId != null) {
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Join my private task on StrangerHelp: https://strangerhelp.com/tasks/$taskId?invite=$inviteCode"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(sendIntent, null)
                            )
                        }
                        navController.popBackStack()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("post_task_submit_button"),
            shape = RoundedCornerShape(26.dp),
            enabled = uiState.isValidForSubmission
        ) {
            Text(
                if (uiState.isPosting) "Posting..." else "Post Task",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
