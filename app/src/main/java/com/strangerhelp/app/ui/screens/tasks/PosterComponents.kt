package com.strangerhelp.app.ui.screens.tasks
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.strangerhelp.app.data.model.ClaimRequest
import com.strangerhelp.app.data.model.ClaimedUser
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.ui.theme.Body
import com.strangerhelp.app.ui.theme.Error
import com.strangerhelp.app.ui.theme.Hairline
import com.strangerhelp.app.ui.theme.Link
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.OnPrimary
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.Surface
import com.strangerhelp.app.ui.theme.SurfaceVariant
import com.strangerhelp.app.ui.theme.Warning
import com.strangerhelp.app.ui.theme.TrustColor
import com.strangerhelp.app.utils.TimeUtils
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

 // Same as AccentGreen


@Composable
fun ClaimRequestCard(
    request: ClaimRequest,
    taskBudget: Int,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isProcessing: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        border = BorderStroke(1.dp, Hairline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = request.requesterName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            request.offeredBudget?.let { offered ->
                val diff = offered - taskBudget
                val diffText = when {
                    diff > 0 -> "(+₹$diff extra)"
                    diff < 0 -> "(₹${-diff} less)"
                    else -> "(same as budget)"
                }
                Text(
                    text = "Asking: ₹$offered $diffText",
                    fontSize = 12.sp,
                    color = when {
                        diff > 0 -> Error
                        diff < 0 -> androidx.compose.ui.graphics.Color(0xFF10B981)
                        else -> androidx.compose.ui.graphics.Color(0xFF666666)
                    }
                )
            }

            request.message?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "\"$it\"",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = Body
                )
            }

            Text(
                text = TimeUtils.getTimeAgo(request.createdAt),
                fontSize = 10.sp,
                color = androidx.compose.ui.graphics.Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF10B981)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("✅ Approve", color = OnPrimary)
                    }
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isProcessing,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("❌ Reject")
                }
            }
        }
    }
}

@Composable
fun ProofReviewSection(
    task: Task,
    onAccept: () -> Unit,
    onReject: (String) -> Unit
) {
    val proofImages = task.completionProof
    var selectedImage by remember { mutableStateOf<String?>(null) }
    var showRejectDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "📋 Completion proof submitted — Review required",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Warning
        )

        // Image Gallery (horizontal scroll)
        if (proofImages.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(proofImages) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Proof image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                // Open full-screen viewer
                                selectedImage = imageUrl
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(
                text = "Tap any image to view full screen",
                fontSize = 11.sp,
                color = androidx.compose.ui.graphics.Color(0xFF666666)
            )
        }

        // Accept / Reject buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF10B981)
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("✅ Accept", color = OnPrimary)
            }
            OutlinedButton(
                onClick = { showRejectDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Error
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("❌ Reject")
            }
        }
        
        if (showRejectDialog) {
            RejectionDialog(
                onDismiss = { showRejectDialog = false },
                onSubmit = { reason ->
                    showRejectDialog = false
                    onReject(reason)
                }
            )
        }
    }

    // Full-screen image viewer dialog
    selectedImage?.let { url ->
        FullScreenImageDialog(
            imageUrl = url,
            onDismiss = { selectedImage = null }
        )
    }
}

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

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
            coil.compose.AsyncImage(
                model = imageUrl,
                contentDescription = "Proof full screen",
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = androidx.compose.ui.geometry.Offset.Zero
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit
            )

            // Close button
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

            // Zoom hint
            Text(
                text = "Pinch to zoom · Tap to close",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}


@Composable
fun RejectionDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        properties = DialogProperties(decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        title = { Text("Reject Proof") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason for rejection") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason) },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Text("Reject", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PrivateTaskInviteLink(
    taskId: String,
    inviteCode: String
) {
    val context = LocalContext.current
    val inviteLink = "https://strangerhelp.com/tasks/$taskId?invite=$inviteCode"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Warning.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, Warning.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔒", fontSize = 18.sp)
                Text(
                    text = "Private Task — Invite Link",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Warning
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = inviteLink,
                    fontSize = 12.sp,
                    color = Link,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Surface)
                        .padding(8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Invite Link", inviteLink)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, "Copy", tint = Primary)
                }
            }

            Text(
                text = "Only people with this link can view and claim.",
                fontSize = 11.sp,
                color = androidx.compose.ui.graphics.Color(0xFF666666)
            )
        }
    }
}

@Composable
fun LiveTrackingSection(
    task: Task,
    modifier: Modifier = Modifier
) {
    val hLat = task.helperLat
    val hLng = task.helperLng
    if (!task.trackingActive || hLat == null || hLng == null) {
        return
    }

    val distance = haversine(
        hLat, hLng,
        task.lat ?: 0.0, task.lng ?: 0.0
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📍 Live Tracking Active",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${"%.1f".format(distance)} km away",
                    fontSize = 12.sp,
                    color = Body
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Hairline)
        ) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    org.maplibre.android.maps.MapView(ctx).apply {
                        setTag("poster_map_${System.currentTimeMillis()}")
                        getMapAsync { map ->
                            try {
                                map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                                    val cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                        .target(org.maplibre.android.geometry.LatLng(hLat, hLng))
                                        .zoom(14.0)
                                        .build()
                                    map.cameraPosition = cameraPosition
                                    val markerOptions = org.maplibre.android.annotations.MarkerOptions()
                                        .position(org.maplibre.android.geometry.LatLng(hLat, hLng))
                                        .title("Helper")
                                    map.addMarker(markerOptions)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

@Composable
fun PosterReviewSection(
    task: Task,
    onReviewSubmit: (rating: Int, comment: String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        border = BorderStroke(1.dp, Hairline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⭐ Rate ${task.claimedByName ?: "Helper"}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(5) { index ->
                    IconButton(
                        onClick = { rating = index + 1 },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star ${index + 1}",
                            tint = if (index < rating) Warning else androidx.compose.ui.graphics.Color(0xFF666666)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Leave a comment (optional)", color = androidx.compose.ui.graphics.Color(0xFF666666)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Button(
                onClick = { 
                    isSubmitting = true
                    onReviewSubmit(rating, comment) 
                },
                enabled = rating > 0 && !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit Review", color = OnPrimary)
                }
            }
        }
    }
}

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}




@Composable
fun EditTaskDialog(
    task: com.strangerhelp.app.data.model.Task,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var budget by remember { mutableStateOf(task.budget.toString()) }

    AlertDialog(
        properties = DialogProperties(decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { char -> char.isDigit() } },
                    label = { Text("Budget (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(mapOf("title" to title, "description" to description, "budget" to (budget.toIntOrNull() ?: 0))) },
                enabled = title.isNotBlank() && description.isNotBlank() && budget.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
