with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),\n            colors =", "colors =")
content = content.replace("colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),\n            border =", "border =")
content = content.replace("shape = RoundedCornerShape(12.dp),\n            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),\n            enabled =", "shape = RoundedCornerShape(12.dp),\n            enabled =")
content = content.replace("shape = RoundedCornerShape(12.dp),\n            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),\n        )", "shape = RoundedCornerShape(12.dp)\n        )")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'w') as f:
    f.write(content)
