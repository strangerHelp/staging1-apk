import re
with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Add import for NotificationViewModel
if "import com.strangerhelp.app.ui.screens.notifications.NotificationViewModel" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.lifecycle.viewmodel.compose.viewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.strangerhelp.app.ui.screens.notifications.NotificationViewModel\nimport com.strangerhelp.app.ui.screens.notifications.NotificationViewModelFactory")

# Remove inline polling block
poll_block_regex = r'    var unreadCount by remember \{ mutableIntStateOf\(0\) \}\n    LaunchedEffect\(Unit\) \{\n        while\(true\) \{\n            try \{\n                val res = com\.strangerhelp\.app\.data\.api\.ApiClient\.api\.getNotifications\(\)\n                if \(res\.isSuccessful\) \{\n                    unreadCount = res\.body\(\)\?\.unreadCount \?: 0\n                \}\n            \} catch \(e: Exception\) \{\}\n            kotlinx\.coroutines\.delay\(10000\)\n        \}\n    \}'

content = re.sub(poll_block_regex, '    val notificationViewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())\n    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()\n\n    LaunchedEffect(Unit) {\n        notificationViewModel.startPolling()\n    }', content)

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
