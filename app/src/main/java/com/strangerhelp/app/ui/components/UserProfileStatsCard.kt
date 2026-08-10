package com.strangerhelp.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.strangerhelp.app.ui.theme.CyanDeep
import com.strangerhelp.app.ui.theme.Warning
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter

@Composable
fun UserProfileStatsCard(
    rating: Float = 4.9f,
    completedTasks: Int = 12,
    completionRate: Float = 0.95f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Trust Level & Reliability", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$rating", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Warning)
                    Text("Rating", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$completedTasks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CyanDeep)
                    Text("Completed", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(completionRate * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Completion", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            Text("Recent Reliability Score", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            
            // A mock history of reliability score for the last 5 tasks
            val scores = entryModelOf(80f, 85f, 95f, 90f, 100f)
            val labels = listOf("T-4", "T-3", "T-2", "T-1", "Current")
            
            val bottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                labels.getOrNull(value.toInt()) ?: ""
            }
            
            Chart(
                chart = columnChart(
                    columns = listOf(lineComponent(color = CyanDeep, thickness = 16.dp, shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(topRightPercent = 50, topLeftPercent = 50)))
                ),
                model = scores,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        }
    }
}
