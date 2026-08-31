import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailRecovered.kt", "r") as f:
    content = f.read()

content = content.replace("fun TaskLifecycleStepper(task: Task) {", "fun TaskLifecycleStepper(task: Task, currentUserId: String?) {")
content = content.replace("fun PaymentNotice(task: Task) {", "fun PaymentNotice(budget: Int) {")
content = content.replace("fun ReviewDialog(\n    task: Task,\n    onDismiss: () -> Unit,\n    onSubmit: (Int, String) -> Unit\n) {", "fun ReviewDialog(\n    onDismiss: () -> Unit,\n    onConfirm: (Int, String) -> Unit\n) {")
content = content.replace("Button(onClick = { onSubmit(rating, comment) }) { Text(\"Submit\") }", "Button(onClick = { onConfirm(rating, comment) }) { Text(\"Submit\") }")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailRecovered.kt", "w") as f:
    f.write(content)
