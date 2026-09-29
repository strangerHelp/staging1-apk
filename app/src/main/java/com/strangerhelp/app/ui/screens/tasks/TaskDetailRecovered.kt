package com.strangerhelp.app.ui.screens.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.data.model.Task

@Composable
fun TaskLifecycleStepper(task: Task, currentUserId: String?) {
    // A simple representation of the lifecycle
    val statuses = listOf("open", "claimed", "completed")
    val currentIdx = statuses.indexOf(task.status).takeIf { it >= 0 } ?: 0
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        statuses.forEachIndexed { index, status ->
            val color = if (index <= currentIdx) Color(0xFF10B981) else Color.LightGray
            Text(status.uppercase(), fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShareSection(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Share Task", fontWeight = FontWeight.Bold)
            Text("Invite friends or helpers.", fontSize = 14.sp)
        }
    }
}

@Composable
fun PaymentNotice(budget: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
        border = BorderStroke(1.dp, Color(0xFFFFE69C)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Direct Payment Notice", fontWeight = FontWeight.Bold, color = Color(0xFF664D03))
            Text("Payments are strictly P2P via UPI. We do not hold funds.", fontSize = 13.sp, color = Color(0xFF664D03))
        }
    }
}

@Composable
fun ReviewDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        properties = androidx.compose.ui.window.DialogProperties(decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        title = { Text("Leave a Review") },
        text = {
            Column {
                Text("Rating (1-5):")
                Slider(value = rating.toFloat(), onValueChange = { rating = it.toInt() }, valueRange = 1f..5f, steps = 3)
                Text("Comment:")
                OutlinedTextField(value = comment, onValueChange = { comment = it })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(rating, comment) }) { Text("Submit") }
        }
    )
}

fun openGoogleMapsDirections(context: Context, lat: Double, lng: Double, location: String?) {
    val uri = Uri.parse("google.navigation:q=$lat,$lng")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng"))
        context.startActivity(browserIntent)
    }
}
