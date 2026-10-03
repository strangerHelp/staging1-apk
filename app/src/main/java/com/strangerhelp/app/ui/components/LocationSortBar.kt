package com.strangerhelp.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocationSortBar(
    locationLabel: String,
    selectedSort: String,
    onSortChanged: (String) -> Unit,
    onLocationClick: () -> Unit = {}
) {
    val sorts = listOf(
        "distance" to "📍 Nearest",
        "newest" to "🆕 Newest",
        "budget_high" to "💰 High ₹",
        "budget_low" to "💸 Low ₹",
        "urgent" to "⚡ Urgent"
    )

    Column(modifier = Modifier.fillMaxWidth().testTag("location_sort_bar")) {
        // Location status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLocationClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = locationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "Change",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("change_location_button")
            )
        }

        // Sort chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(sorts) { (key, label) ->
                val isSelected = selectedSort == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onSortChanged(key) },
                    label = {
                        Text(
                            label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("sort_chip_$key")
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
