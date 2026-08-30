import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

old_switch = """        val darkSwitchColors = SwitchDefaults.colors(
            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline
        )"""

new_switch = """        val darkSwitchColors = SwitchDefaults.colors(
            uncheckedTrackColor = MaterialTheme.colorScheme.surface,
            uncheckedBorderColor = MaterialTheme.colorScheme.onSurface,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
        )"""

content = content.replace(old_switch, new_switch)

custom_tf_colors = "colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))"
content = content.replace("shape = RoundedCornerShape(12.dp),", f"shape = RoundedCornerShape(12.dp),\n            {custom_tf_colors},")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'w') as f:
    f.write(content)

print("Patched!")
