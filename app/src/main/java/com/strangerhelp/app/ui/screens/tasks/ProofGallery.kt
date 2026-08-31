package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.strangerhelp.app.R
import com.strangerhelp.app.ui.theme.Body
import com.strangerhelp.app.ui.theme.Error
import com.strangerhelp.app.ui.theme.Hairline
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.OnPrimary
import com.strangerhelp.app.ui.theme.TrustColor

@Composable
fun ProofGallery(
    proof: List<String>,
    canReview: Boolean = false,
    onAccept: () -> Unit = {},
    onReject: (String) -> Unit = {}
) {
    if (proof.isEmpty()) return

    var viewer by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section header
        Text(
            text = "📸 COMPLETION PROOF",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Muted,
            letterSpacing = 0.5.sp
        )

        // Thumbnail gallery
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(proof) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Completion proof",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Hairline, RoundedCornerShape(8.dp))
                        .clickable { viewer = url },
                    contentScale = ContentScale.Crop,
                    // Use standard Compose icon for placeholder since we don't know if R.drawable.image_placeholder exists
                    // placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                )
            }
        }

        Text(
            text = "Tap any image to view full screen",
            fontSize = 11.sp,
            color = Muted
        )

        // Poster review actions
        if (canReview) {
            Spacer(modifier = Modifier.height(4.dp))

            var showRejectDialog by remember { mutableStateOf(false) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrustColor
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text("✅ Accept & Complete", color = OnPrimary)
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

            // Rejection Dialog
            if (showRejectDialog) {
                RejectReasonDialog(
                    onDismiss = { showRejectDialog = false },
                    onConfirm = { reason ->
                        showRejectDialog = false
                        onReject(reason)
                    }
                )
            }
        }
    }

    // Full-screen viewer
    viewer?.let { url ->
        FullScreenProofViewer(
            dataUrl = url,
            onDismiss = { viewer = null }
        )
    }
}

@Composable
fun FullScreenProofViewer(
    dataUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
                .background(Color.Black.copy(alpha = 0.96f))
                .clickable { onDismiss() }
        ) {
            // Zoomable image
            AsyncImage(
                model = dataUrl,
                contentDescription = "Proof (zoomable)",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset += pan
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
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
                text = "Pinch to zoom · Tap ✕ to close",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
fun RejectReasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Why are you rejecting this proof?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Please provide a reason for rejection:",
                    fontSize = 13.sp,
                    color = Body
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = {
                        Text(
                            "e.g., Photo unclear, wrong document, task not fully done",
                            color = Muted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    isError = reason.isBlank()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reason.ifBlank { "No reason provided" })
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Error
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("Reject Proof")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
