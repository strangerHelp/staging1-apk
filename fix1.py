with open('app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("unfocusedBorderColor = MaterialTheme.colorScheme.outline) Color(0xFFB3261E) else Color(0xFFCCCCCC)", "unfocusedBorderColor = if (passwordsDoNotMatch) Color(0xFFB3261E) else MaterialTheme.colorScheme.outline")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/auth/ResetPasswordScreen.kt', 'w') as f:
    f.write(content)
