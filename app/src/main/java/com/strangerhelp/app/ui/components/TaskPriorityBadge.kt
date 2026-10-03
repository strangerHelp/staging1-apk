package com.strangerhelp.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.data.model.TaskPriority

data class PriorityColorConfig(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val icon: ImageVector,
    val label: String
)

fun getPriorityColorConfig(priority: TaskPriority): PriorityColorConfig {
    return when (priority) {
        TaskPriority.HIGH -> PriorityColorConfig(
            backgroundColor = Color(0xFFFEE2E2), // Soft Crimson
            contentColor = Color(0xFFDC2626),    // Dark Red
            borderColor = Color(0xFFFCA5A5),
            icon = Icons.Default.Bolt,
            label = "High Urgency"
        )
        TaskPriority.MEDIUM -> PriorityColorConfig(
            backgroundColor = Color(0xFFFEF3C7), // Warm Amber
            contentColor = Color(0xFFD97706),    // Dark Amber
            borderColor = Color(0xFFFCD34D),
            icon = Icons.Default.AccessTime,
            label = "Medium Urgency"
        )
        TaskPriority.LOW -> PriorityColorConfig(
            backgroundColor = Color(0xFFECFDF5), // Soft Emerald
            contentColor = Color(0xFF059669),    // Emerald Green
            borderColor = Color(0xFFA7F3D0),
            icon = Icons.Default.CheckCircleOutline,
            label = "Low Urgency"
        )
    }
}

@Composable
fun TaskPriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    val config = getPriorityColorConfig(priority)
    val testTagValue = when (priority) {
        TaskPriority.HIGH -> "priority_badge_high"
        TaskPriority.MEDIUM -> "priority_badge_medium"
        TaskPriority.LOW -> "priority_badge_low"
    }

    Surface(
        color = config.backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, config.borderColor),
        modifier = modifier.testTag(testTagValue)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 12.dp else 14.dp),
                tint = config.contentColor
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = config.label,
                fontSize = if (compact) 11.sp else 13.sp,
                color = config.contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
