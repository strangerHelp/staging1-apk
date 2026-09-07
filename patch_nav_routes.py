import re

file_path = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add AskViewModel definition just after notificationViewModel
vm_def = """
                composable("ask") {
                    AskListScreen(viewModel = askViewModel, navController = navController)
                }
                composable("ask_post") {
                    AskPostScreen(viewModel = askViewModel, navController = navController)
                }
                composable("ask_detail/{questionId}") { backStackEntry ->
                    val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
                    AskDetailScreen(
                        questionId = questionId, viewModel = askViewModel, navController = navController
                    )
                }
"""

content = content.replace("composable(\"pulse\") { PulseScreen(navController) }",
                          "composable(\"pulse\") { PulseScreen(navController) }\n" + vm_def)


with open(file_path, "w") as f:
    f.write(content)
