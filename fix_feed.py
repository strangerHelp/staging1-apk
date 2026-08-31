import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "r") as f:
    content = f.read()

# Add imports for ViewModel and collectAsStateWithLifecycle
if "import androidx.lifecycle.viewmodel.compose.viewModel" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.lifecycle.viewmodel.compose.viewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.strangerhelp.app.ui.screens.notifications.NotificationViewModel\nimport com.strangerhelp.app.ui.screens.notifications.NotificationViewModelFactory")

# Add NotificationViewModel to FeedScreen params
content = re.sub(r'fun FeedScreen\(navController: NavController, user: User\?\) \{', 'fun FeedScreen(\n    navController: NavController,\n    user: User?,\n    notificationViewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())\n) {', content)

# Collect unread count
collect_str = "    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()\n"
if "val unreadCount by notificationViewModel.unreadCount" not in content:
    content = re.sub(r'(fun FeedScreen\([^)]+\) \{\n)', r'\1' + collect_str, content)

# Update HomeHeader call to use unreadCount from ViewModel, not hardcoded 3
content = re.sub(r'HomeHeader\(user = user, unreadCount = 3\) \{', 'HomeHeader(user = user, unreadCount = unreadCount) {', content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "w") as f:
    f.write(content)
