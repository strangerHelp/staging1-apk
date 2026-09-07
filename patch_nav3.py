import re

file_path = "app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add AskViewModel definition just after notificationViewModel
vm_def = """    val askViewModel: com.strangerhelp.app.ui.screens.ask.AskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.strangerhelp.app.ui.screens.ask.AskViewModelFactory(
            com.strangerhelp.app.data.repository.AskRepository(com.strangerhelp.app.data.api.ApiClient.api),
            com.strangerhelp.app.data.repository.AuthRepository(com.strangerhelp.app.data.api.ApiClient.api, sharedPrefs)
        )
    )
"""
content = content.replace("    val notificationViewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())",
                          "    val notificationViewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())\n" + vm_def)

# Update the composables to pass the viewModel
content = content.replace("                    AskListScreen(\n                        navController = navController\n                    )", 
                          "                    AskListScreen(viewModel = askViewModel, navController = navController)")

content = content.replace("                    AskPostScreen(\n                        navController = navController\n                    )", 
                          "                    AskPostScreen(viewModel = askViewModel, navController = navController)")

content = content.replace("                    AskDetailScreen(\n                        questionId = questionId,\n                        navController = navController\n                    )", 
                          "                    AskDetailScreen(questionId = questionId, viewModel = askViewModel, navController = navController)")


with open(file_path, "w") as f:
    f.write(content)
