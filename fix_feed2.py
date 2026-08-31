import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "r") as f:
    content = f.read()

# Replace FeedScreen signature and add NotificationViewModel
content = re.sub(
    r'fun FeedScreen\(\n    navController: NavController,\n    user: User\?,\n    viewModel: FeedViewModel = viewModel\(\)\n\) \{',
    'fun FeedScreen(\n    navController: NavController,\n    user: User?,\n    viewModel: FeedViewModel = viewModel(),\n    notificationViewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())\n) {',
    content
)

# Insert unreadCount extraction
content = content.replace(
    '    val isLoading by viewModel.isLoading.collectAsState()\n',
    '    val isLoading by viewModel.isLoading.collectAsState()\n    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()\n'
)

# And make sure HomeHeader uses it
content = content.replace('HomeHeader(user = user, unreadCount = 3)', 'HomeHeader(user = user, unreadCount = unreadCount)')

with open("app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt", "w") as f:
    f.write(content)
