import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Add import
if "import androidx.lifecycle.viewmodel.compose.viewModel" not in content:
    content = content.replace("import androidx.navigation.compose.rememberNavController", "import androidx.navigation.compose.rememberNavController\nimport androidx.lifecycle.viewmodel.compose.viewModel\nimport com.strangerhelp.app.ui.screens.chat.ChatViewModel")

# Add viewModel instantiation
content = content.replace("val navController = rememberNavController()", "val navController = rememberNavController()\n    val chatViewModel: ChatViewModel = viewModel()")

# Pass to screens
content = content.replace("composable(Screen.Chat.route) { ChatListScreen(navController = navController) }", "composable(Screen.Chat.route) { ChatListScreen(viewModel = chatViewModel, navController = navController) }")
content = content.replace("ChatDetailScreen(conversationId = entry.arguments?.getString(\"convId\") ?: \"\", navController = navController)", "ChatDetailScreen(conversationId = entry.arguments?.getString(\"convId\") ?: \"\", viewModel = chatViewModel, navController = navController)")

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
