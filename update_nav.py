import re

code = """
import com.strangerhelp.app.ui.screens.path.PathSetupScreen
import com.strangerhelp.app.ui.screens.path.PathActiveScreen
"""

with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

# Add imports
content = content.replace('import com.strangerhelp.app.ui.screens.notifications.NotificationsScreen', 'import com.strangerhelp.app.ui.screens.notifications.NotificationsScreen\nimport com.strangerhelp.app.ui.screens.path.PathSetupScreen\nimport com.strangerhelp.app.ui.screens.path.PathActiveScreen')

# Add routes
routes = """
                composable("path_setup") { PathSetupScreen(navController) }
                composable("path_active") { PathActiveScreen(navController) }
"""

content = content.replace('composable("notifications") { NotificationsScreen(navController) }', 'composable("notifications") { NotificationsScreen(navController) }\n' + routes)

with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
