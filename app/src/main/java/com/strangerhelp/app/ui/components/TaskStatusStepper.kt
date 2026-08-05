package com.strangerhelp.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.ui.theme.CyanDeep
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.Warning

@Composable
fun TaskStatusStepper(status: String) {
    val steps = listOf("Open", "Claimed", "Pending", "Completed")
    val currentIndex = when (status.lowercase()) {
        "open" -> 0
        "claimed" -> 1
        "completion_pending" -> 2
        "completed", "accepted" -> 3
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, stepName ->
            val isCompleted = index <= currentIndex
            val isCurrent = index == currentIndex
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (isCompleted) CyanDeep else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            color = Muted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stepName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface else Muted,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            if (index < steps.size - 1) {
                // Line
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (index < currentIndex) CyanDeep else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}
