import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "r") as f:
    content = f.read()

# Replace the runtime import to also add NotificationViewModel imports
content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.lifecycle.viewmodel.compose.viewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.strangerhelp.app.ui.screens.notifications.NotificationViewModel\nimport com.strangerhelp.app.ui.screens.notifications.NotificationViewModelFactory")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "w") as f:
    f.write(content)
