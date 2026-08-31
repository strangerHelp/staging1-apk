import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

# Fix the dangling braces
content = content.replace("        }\n    }\n}\n\n@Composable\nfun PrivateTaskInviteLink", "\n@Composable\nfun PrivateTaskInviteLink")

rejection_dialog_code = """
@Composable
fun RejectionDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject Proof") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason for rejection") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason) },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Text("Reject", color = Color.White)
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

if "fun RejectionDialog" not in content:
    content = content.replace("@Composable\nfun PrivateTaskInviteLink", rejection_dialog_code + "\n@Composable\nfun PrivateTaskInviteLink")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)
