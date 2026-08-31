with open("app/src/main/java/com/strangerhelp/app/ui/screens/notifications/NotificationsScreen.kt", "r") as f:
    content = f.read()

# AccentOrange missing import or definition, just use primary
content = content.replace('color = AccentOrange', 'color = MaterialTheme.colorScheme.primary')
content = content.replace('import com.strangerhelp.app.ui.theme.AccentOrange\n', '')
content = content.replace('import com.strangerhelp.app.ui.theme.DarkGray\n', '')

with open("app/src/main/java/com/strangerhelp/app/ui/screens/notifications/NotificationsScreen.kt", "w") as f:
    f.write(content)
