package com.strangerhelp.app.ui.screens.meets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.data.model.Attendee
import com.strangerhelp.app.data.model.Meet
import com.strangerhelp.app.data.model.User
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.filled.ContentCopy
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.Locale

val Primary = Color(0xFF171717)
val Body = Color(0xFF666666)
val Muted = Color(0xFF999999)
val Surface = Color.White
val SurfaceVariant = Color(0xFFF5F5F5)
val Hairline = Color(0xFFE5E5E5)
val TrustColor = Color(0xFF10B981)
val Warning = Color(0xFFEE0000)
val Link = Color(0xFF0056b3)

fun formatDate(dateStr: String): String {
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = parser.parse(dateStr)
        if (date != null) {
            return formatter.format(date)
        }
    } catch (e: Exception) {}
    return dateStr
}

@Composable
fun CategoryChip(label: String, containerColor: Color = SurfaceVariant, textColor: Color = Primary) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MeetCard(
    meet: Meet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChip(label = meet.category, containerColor = SurfaceVariant)
                if (meet.visibility == "private") {
                    CategoryChip(
                        label = "🔒 Private",
                        containerColor = Warning.copy(alpha = 0.12f),
                        textColor = Warning
                    )
                }
            }

            Text(
                text = meet.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary
            )

            Text(
                text = "📍 ${meet.location}",
                fontSize = 12.sp,
                color = Muted
            )

            Text(
                text = "📅 ${formatDate(meet.date)} · ${meet.time}",
                fontSize = 12.sp,
                color = Muted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 ${meet.attendeeCount}/${meet.maxAttendees} joined",
                    fontSize = 12.sp,
                    color = TrustColor
                )
                Text(
                    text = "Hosted by ${if (meet.anonymous == 1) "Anonymous" else meet.hostName}",
                    fontSize = 11.sp,
                    color = Muted
                )
            }
        }
    }
}

@Composable
fun MeetInfoCard(meet: Meet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Hairline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CategoryChip(label = meet.category)
                if (meet.visibility == "private") {
                    CategoryChip(label = "🔒 Private", containerColor = Warning.copy(alpha = 0.12f), textColor = Warning)
                }
            }
            Text(text = meet.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
            Text(text = "📍 ${meet.location}", fontSize = 13.sp, color = Body)
            Text(text = "📅 ${formatDate(meet.date)} · ${meet.time}", fontSize = 13.sp, color = Body)
            Text(text = "👥 ${meet.attendeeCount}/${meet.maxAttendees} joined", fontSize = 13.sp, color = TrustColor)
            Text(text = "Hosted by ${if (meet.anonymous == 1) "Anonymous" else meet.hostName}", fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable
fun AttendeesSection(attendees: List<Attendee>, attendeeCount: Int, maxAttendees: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Hairline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "── Attendees ($attendeeCount/$maxAttendees) ──", fontSize = 12.sp, color = Muted, letterSpacing = 0.5.sp)
            if (attendees.isEmpty()) {
                Text("No one has joined yet. Be the first!", fontSize = 13.sp, color = Muted)
            } else {
                val displayAttendees = attendees.take(5)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayAttendees.forEach { attendee ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = attendee.userName.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                    if (attendees.size > 5) {
                        Text(text = "+${attendees.size - 5} more", fontSize = 11.sp, color = Muted)
                    }
                }
            }
        }
    }
}

@Composable
fun PrivateInviteLink(meetId: String, inviteCode: String) {
    val context = LocalContext.current
    val inviteLink = "https://strangerhelp.com/meets/$meetId?code=$inviteCode"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, Warning.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔒", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Private Meet — Invite Link", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Warning)
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
        }
    }
}

@Composable
fun MeetActions(
    meet: Meet,
    currentUser: User?,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onMessageHost: () -> Unit,
    onLoginClick: () -> Unit
) {
    val isHost = currentUser?.id == meet.hostId
    val isAttendee = meet.attendees.any { it.userId == currentUser?.id }
    val isFull = meet.attendeeCount >= meet.maxAttendees
    val spotsLeft = meet.maxAttendees - meet.attendeeCount

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        when {
            currentUser == null -> {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Login to Join", color = Color.White)
                }
            }
            isHost -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TrustColor.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "You're hosting this meet",
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        fontSize = 14.sp,
                        color = TrustColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
            isAttendee -> {
                OutlinedButton(
                    onClick = onLeave,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Warning)
                ) {
                    Text("Leave Meet")
                }
            }
            isFull -> {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Meet is Full")
                }
            }
            else -> {
                Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Join Meet ($spotsLeft spots left)", color = Color.White)
                }
            }
        }

        if (!isHost && currentUser != null && meet.hostId != null) {
            OutlinedButton(
                onClick = onMessageHost,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("💬 Message Host")
            }
        }
    }
}
