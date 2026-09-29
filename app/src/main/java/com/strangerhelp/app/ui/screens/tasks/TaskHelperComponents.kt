package com.strangerhelp.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.repository.TaskRepository
import android.content.Context
import android.content.Intent
import com.strangerhelp.app.data.model.Task

















@Composable
fun LoginPrompt(navController: NavController) {
    Button(
        onClick = { navController.navigate("login") },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5A623))
    ) {
        Text("Login to Request", color = Color.White)
    }
}

@Composable
fun ClaimDialog(
    taskBudget: Int,
    isGroupTask: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isLoading: Boolean
) {
    var budget by remember { mutableStateOf(taskBudget.toString()) }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        properties = androidx.compose.ui.window.DialogProperties(decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        title = { Text(if (isGroupTask) "Request to Join" else "Request to Claim") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "You can accept the proposed budget or suggest a different amount.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { c -> c.isDigit() } },
                    label = { Text("Your Offer (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (Optional)") },
                    placeholder = { Text("e.g. I can do this right now.") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(budget.toIntOrNull() ?: taskBudget, message) },
                enabled = !isLoading && budget.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5A623))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Submit Request", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

class TaskDetailViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = ApiClient.api
        val db = try { com.strangerhelp.app.StrangerHelpApp.instance.database } catch (_: Exception) { null }
        val repository = TaskRepository(api, db?.taskDao())
        @Suppress("UNCHECKED_CAST")
        return TaskDetailViewModel(repository) as T
    }
}





fun shareTask(context: Context, task: Task) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Check out this task on StrangerHelp: ${task.title} for ₹${task.budget}!\nhttps://strangerhelp.com/tasks/${task._id}")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}
