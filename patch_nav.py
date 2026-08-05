import os

path = 'app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace(
    'object Feed : Screen("feed", "Feed", Icons.Filled.Explore, Icons.Outlined.Explore)',
    'object Feed : Screen("feed", "Home", Icons.Filled.Home, Icons.Outlined.Home)'
)

content = content.replace(
    'object Tasks : Screen("tasks", "Tasks", Icons.Filled.Assignment, Icons.Outlined.Assignment)',
    'object Tasks : Screen("tasks", "Tasks", Icons.Filled.Explore, Icons.Outlined.Explore)'
)

content = content.replace(
    'object Chat : Screen("chat", "Chat", Icons.Filled.Chat, Icons.Outlined.Chat)',
    'object Chat : Screen("chat", "Messages", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline)'
)

content = content.replace(
    'object Profile : Screen("profile", "Me", Icons.Filled.Person, Icons.Outlined.Person)',
    'object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)'
)

content = content.replace(
    'val bottomNavItems = listOf(Screen.Feed, Screen.Tasks, Screen.Post, Screen.Chat, Screen.Profile)',
    'val bottomNavItems = listOf(Screen.Feed, Screen.Tasks, Screen.Chat, Screen.Profile)'
)

with open(path, 'w') as f:
    f.write(content)
