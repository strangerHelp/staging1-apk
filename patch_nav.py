import re

with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

import_statement = "import com.strangerhelp.app.ui.screens.WebViewScreen\n"

if import_statement not in content:
    content = content.replace('import androidx.compose.runtime.Composable', import_statement + 'import androidx.compose.runtime.Composable')

route = """
                composable(
                    "webview?url={url}",
                    arguments = listOf(navArgument("url") { type = NavType.StringType })
                ) { entry ->
                    val url = entry.arguments?.getString("url") ?: ""
                    WebViewScreen(url = url, navController = navController)
                }
"""

content = content.replace('composable("notifications") { NotificationsScreen(navController) }', 'composable("notifications") { NotificationsScreen(navController) }' + route)

with open('app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
