import re

file_path = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add imports
imports = """import com.strangerhelp.app.ui.screens.ask.AskListScreen
import com.strangerhelp.app.ui.screens.ask.AskPostScreen
import com.strangerhelp.app.ui.screens.ask.AskDetailScreen"""

content = content.replace("import com.strangerhelp.app.ui.screens.feed.FeedScreen", imports + "\nimport com.strangerhelp.app.ui.screens.feed.FeedScreen")

# Add routes
routes = """
                composable("ask") {
                    AskListScreen(
                        navController = navController
                    )
                }
                composable("ask_post") {
                    AskPostScreen(
                        navController = navController
                    )
                }
                composable("ask_detail/{questionId}") { backStackEntry ->
                    val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
                    AskDetailScreen(
                        questionId = questionId,
                        navController = navController
                    )
                }
"""

content = content.replace("composable(Screen.Path.route) { ComingSoonScreen(\"Path\") }", 
                          "composable(Screen.Path.route) { ComingSoonScreen(\"Path\") }\n" + routes)

with open(file_path, "w") as f:
    f.write(content)
