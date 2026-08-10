package com.strangerhelp.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.strangerhelp.app.ui.theme.CyanDeep
import com.strangerhelp.app.ui.theme.Warning
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.lineComponent
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter

@Composable
fun DashboardChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Activity & Rewards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May")
            val completions = entryModelOf(5f, 8f, 12f, 6f, 10f)
            val rewards = entryModelOf(500f, 800f, 1500f, 600f, 1200f)
            
            val bottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                months.getOrNull(value.toInt()) ?: ""
            }
            
            Text("Tasks Completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Chart(
                chart = columnChart(
                    columns = listOf(lineComponent(color = CyanDeep, thickness = 16.dp, shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(topRightPercent = 50, topLeftPercent = 50)))
                ),
                model = completions,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text("Rewards Earned (₹)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Chart(
                chart = lineChart(
                    lines = listOf(
                        com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                            lineColor = Warning,
                            lineThickness = 3.dp
                        )
                    )
                ),
                model = rewards,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
        }
    }
}
