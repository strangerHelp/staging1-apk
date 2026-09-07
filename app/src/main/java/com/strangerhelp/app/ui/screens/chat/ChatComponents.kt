package com.strangerhelp.app.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.strangerhelp.app.R
import com.strangerhelp.app.data.model.Message
import com.strangerhelp.app.ui.theme.*
import com.strangerhelp.app.utils.compressAndSaveImage
import java.io.File

@Composable
fun MessageBubble(
    message: Message,
    isOwn: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isOwn) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Mock avatar for non-own user, could use a real image if available
                AsyncImage(
                    model = "https://i.pravatar.cc/150?u=${message.senderName}",
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_launcher_foreground_image)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOwn) Color.Black else Color.White
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isOwn) 12.dp else 4.dp,
                bottomEnd = if (isOwn) 4.dp else 12.dp
            ),
            border = if (!isOwn) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.attachments.isNotEmpty()) {
                    message.attachments.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Attachment",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = if (isOwn) Color.White else Color.Black,
                        fontSize = 15.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.strangerhelp.app.utils.TimeUtils.formatMessageTime(message.createdAt),
                        fontSize = 11.sp,
                        color = if (isOwn) Color.White.copy(alpha = 0.7f) else Muted
                    )
                    
                    if (isOwn) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(android.R.drawable.checkbox_on_background), 
                            contentDescription = "Read",
                            tint = Saffron,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        
        if (isOwn) {
            Spacer(modifier = Modifier.width(36.dp)) // To align with left side avatar if needed, but mockup doesn't have right avatar.
        }
    }
}

val QUICK_REPLIES = listOf(
    "Available now",
    "On my way",
    "Task completed ✓"
)

@Composable
fun QuickReplies(onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(QUICK_REPLIES) { reply ->
            val isCompleted = reply == "Task completed ✓"
            Button(
                onClick = { onSelect(reply) },
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                border = if (isCompleted) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) Saffron else Color.White,
                    contentColor = if (isCompleted) Color.Black else Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = reply,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: (File) -> Unit,
    isSending: Boolean = false,
    maxLength: Int = 5000
) {
    val context = LocalContext.current
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val file = compressAndSaveImage(context, uri)
                file?.let { onAttach(it) }
            } catch (e: Exception) {
            }
        }
    }

    val charCount = text.length
    val isNearLimit = charCount > maxLength - 500
    val isOverLimit = charCount > maxLength

    Surface(
        color = BackgroundLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { pickImage.launch("image/*") },
                    modifier = Modifier.size(40.dp),
                    enabled = !isSending
                ) {
                    Text("📎", fontSize = 20.sp, color = if (isSending) Muted else Color.Black)
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            text = "Type a message...",
                            fontSize = 15.sp,
                            color = Muted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 120.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 4,
                    isError = isOverLimit,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { if (!isOverLimit && text.isNotBlank()) onSend() }
                    ),
                    singleLine = false
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank() && !isOverLimit && !isSending,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (text.isNotBlank() && !isOverLimit && !isSending) Color.Black else MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            if (isNearLimit) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${charCount}/${maxLength}",
                        fontSize = 10.sp,
                        color = if (isOverLimit) Color.Red else Muted,
                        modifier = Modifier.padding(end = 12.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}
