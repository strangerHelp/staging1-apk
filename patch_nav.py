import os

path = 'app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''    val snackbarHostState = remember { SnackbarHostState() }

    androidx.compose.runtime.CompositionLocalProvider(com.strangerhelp.app.ui.components.LocalSnackbarHostState provides snackbarHostState) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(if (selected) screen.icon else screen.iconOutlined, screen.label) },
                            label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->'''
    
replacement = '''    val snackbarHostState = remember { SnackbarHostState() }
    
    var unreadCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while(true) {
            try {
                val res = com.strangerhelp.app.data.api.ApiClient.api.getNotifications()
                if (res.isSuccessful) {
                    unreadCount = res.body()?.notifications?.count { !it.read } ?: 0
                }
            } catch (e: Exception) {}
            kotlinx.coroutines.delay(10000)
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(com.strangerhelp.app.ui.components.LocalSnackbarHostState provides snackbarHostState) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { 
                                if (screen == Screen.Chat && unreadCount > 0) {
                                    BadgedBox(badge = { Badge { Text(unreadCount.toString()) } }) {
                                        Icon(if (selected) screen.icon else screen.iconOutlined, screen.label)
                                    }
                                } else {
                                    Icon(if (selected) screen.icon else screen.iconOutlined, screen.label)
                                }
                            },
                            label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->'''

content = content.replace(target, replacement)

target2 = '''import androidx.compose.ui.Modifier'''
replacement2 = '''import androidx.compose.ui.Modifier
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.mutableIntStateOf'''
content = content.replace(target2, replacement2)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated AppNavigation unread badge")
