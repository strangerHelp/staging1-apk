edit_dialog = """
@Composable
fun EditTaskDialog(
    task: com.strangerhelp.app.data.model.Task,
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, budget: String) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var budget by remember { mutableStateOf(task.budget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { char -> char.isDigit() } },
                    label = { Text("Budget (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(title, description, budget) },
                enabled = title.isNotBlank() && description.isNotBlank() && budget.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
"""

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "a") as f:
    f.write(edit_dialog)
