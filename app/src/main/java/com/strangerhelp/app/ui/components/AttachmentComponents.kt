package com.strangerhelp.app.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.strangerhelp.app.R
import kotlinx.coroutines.delay
import java.io.File

// --- AttachmentType.kt ---
enum class AttachmentType {
    IMAGE, AUDIO, VIDEO, PDF, FILE
}

fun getAttachmentType(dataUrl: String): AttachmentType = when {
    dataUrl.startsWith("data:image") -> AttachmentType.IMAGE
    dataUrl.startsWith("data:audio") -> AttachmentType.AUDIO
    dataUrl.startsWith("data:video") -> AttachmentType.VIDEO
    dataUrl.startsWith("data:application/pdf") -> AttachmentType.PDF
    else -> AttachmentType.FILE
}

fun isImageAttachment(dataUrl: String): Boolean = dataUrl.startsWith("data:image")
fun isAudioAttachment(dataUrl: String): Boolean = dataUrl.startsWith("data:audio")
fun isPdfAttachment(dataUrl: String): Boolean = dataUrl.startsWith("data:application/pdf")

// --- AttachmentsSection.kt ---
@Composable
fun AttachmentsSection(
    attachments: List<String>,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return
    
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header
        Text(
            text = "📎 Attachments",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280), // Muted
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Separate attachments by type
        val images = attachments.filter { isImageAttachment(it) }
        val audios = attachments.filter { isAudioAttachment(it) }
        val files = attachments.filter {
            !isImageAttachment(it) && !isAudioAttachment(it)
        }

        // ⭐ Images Grid (Thumbnails)
        if (images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(images) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Attachment",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                fullScreenImageUrl = url
                            }
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop,
                        // Using background instead of placeholder to avoid missing R resources
                    )
                }
            }
        }

        // ⭐ Voice Notes (Audio)
        if (audios.isNotEmpty()) {
            audios.forEach { url ->
                VoiceNotePlayer(
                    dataUrl = url,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ⭐ PDFs / Other Files
        if (files.isNotEmpty()) {
            files.forEachIndexed { index, url ->
                OutlinedButton(
                    onClick = { /* openFile(url) */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Document ${index + 1}")
                }
            }
        }
    }
    
    fullScreenImageUrl?.let { url ->
        FullScreenImageDialog(
            dataUrl = url,
            onDismiss = { fullScreenImageUrl = null }
        )
    }
}

// --- VoiceNotePlayer.kt ---
@Composable
fun VoiceNotePlayer(
    dataUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf(0) }
    var currentPosition by remember { mutableStateOf(0) }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setOnCompletionListener {
                isPlaying = false
                currentPosition = 0
            }
            setOnPreparedListener {
                duration = it.duration / 1000
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val file = decodeBase64ToFile(context, dataUrl, "webm")
            mediaPlayer.apply {
                reset()
                setDataSource(file.absolutePath)
                prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (mediaPlayer.isPlaying) {
                currentPosition = mediaPlayer.currentPosition / 1000
            }
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Waveform Icon (using GraphicEq replacement or similar)
            Icon(
                Icons.Default.GraphicEq,
                contentDescription = null,
                tint = Color(0xFF00BFA5), // TrustColor
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Duration
            Text(
                text = formatDuration(if (isPlaying) currentPosition else duration),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Progress Bar (Simplified)
            LinearProgressIndicator(
                progress = { if (duration > 0) currentPosition.toFloat() / duration else 0f },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF00BFA5),
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Play/Pause Button
            IconButton(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        mediaPlayer.start()
                        isPlaying = true
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun decodeBase64ToFile(context: Context, dataUrl: String, extension: String): File {
    val base64 = dataUrl.substringAfter("base64,")
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.$extension")
    file.writeBytes(bytes)
    return file
}

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

// --- FullScreenImageDialog.kt ---
@Composable
fun FullScreenImageDialog(
    dataUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = dataUrl,
                contentDescription = "Full screen image",
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 4f)
                            offsetX += pan.x * scale
                            offsetY += pan.y * scale
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    },
                contentScale = ContentScale.Fit
            )

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Zoom Instructions
            Text(
                text = "Pinch to zoom • Tap to close",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}
