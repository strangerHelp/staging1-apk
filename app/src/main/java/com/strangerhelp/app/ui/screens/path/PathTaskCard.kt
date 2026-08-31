package com.strangerhelp.app.ui.screens.path

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.data.model.PathTask
import com.strangerhelp.app.ui.screens.tasks.CategoryChip
import com.strangerhelp.app.ui.theme.CyanDeep
import com.strangerhelp.app.ui.theme.Error
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.SurfaceVariant
import com.strangerhelp.app.ui.theme.TrustColor

@Composable
fun PathTaskCard(
    task: PathTask,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top row: Category + Urgent + Budget
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryChip(
                    label = task.category,
                    containerColor = SurfaceVariant,
                    textColor = Muted
                )
                if (task.urgent == 1) {
                    CategoryChip(
                        label = "⚡ Urgent",
                        containerColor = Error.copy(alpha = 0.12f),
                        textColor = Error
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "₹${task.budget}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CyanDeep
                )
            }

            // Title
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Distance metrics
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${"%.1f".format(task.distFromStart)} km along",
                    fontSize = 11.sp,
                    color = TrustColor
                )
                Text(
                    text = "↕ ${"%.1f".format(task.offPath)} km off route",
                    fontSize = 11.sp,
                    color = Muted
                )
            }

            // Poster
            Text(
                text = "Posted by ${task.posterName ?: "Unknown"}",
                fontSize = 11.sp,
                color = Muted
            )
        }
    }
}
