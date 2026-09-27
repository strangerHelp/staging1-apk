package com.strangerhelp.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Reusable user avatar component that replaces demo face photos with clean,
 * dynamic initials badges and displays real user avatars when uploaded.
 */
@Composable
fun UserAvatar(
    avatarUrl: String?,
    name: String?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    showEditBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    // Treat demo pravatar/placeholder images as empty so user's real initial is shown
    val cleanUrl = avatarUrl?.trim()?.takeIf {
        it.isNotEmpty() && !it.contains("pravatar.cc") && !it.contains("randomuser.me")
    }

    val initialColor = rememberAvatarColor(name ?: "")
    val initialLetter = (name?.trim()?.firstOrNull() ?: 'U').uppercaseChar()

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!cleanUrl.isNullOrEmpty()) {
            AsyncImage(
                model = cleanUrl,
                contentDescription = name ?: "User Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Elegant initials badge
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(initialColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialLetter.toString(),
                    color = Color.White,
                    fontSize = (size.value * 0.42f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showEditBadge) {
            val badgeSize = (size * 0.35f).coerceIn(24.dp, 36.dp)
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFB340))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Upload or change profile picture",
                    tint = Color(0xFF1F1F1F),
                    modifier = Modifier.size(badgeSize * 0.6f)
                )
            }
        }
    }
}

@Composable
private fun rememberAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF2A9D8F), // Teal
        Color(0xFFE76F51), // Burnt Orange
        Color(0xFF457B9D), // Slate Blue
        Color(0xFF6A4C93), // Purple
        Color(0xFF1982C4), // Ocean Blue
        Color(0xFFD4A373), // Warm Sand
        Color(0xFF00897B)  // Emerald
    )
    val hash = kotlin.math.abs(name.hashCode())
    return colors[hash % colors.size]
}
