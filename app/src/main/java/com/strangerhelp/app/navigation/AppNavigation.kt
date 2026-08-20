package com.strangerhelp.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.ui.screens.chat.ChatDetailScreen
import com.strangerhelp.app.ui.screens.chat.ChatListScreen
import com.strangerhelp.app.ui.screens.feed.FeedScreen
import com.strangerhelp.app.ui.screens.post.PostTaskScreen
import com.strangerhelp.app.ui.screens.post.PostMeetScreen
import com.strangerhelp.app.ui.screens.post.PostQuestionScreen
import com.strangerhelp.app.ui.screens.profile.ProfileScreen
import com.strangerhelp.app.ui.screens.profile.EditProfileScreen
import com.strangerhelp.app.ui.screens.tasks.TaskDetailScreen
import com.strangerhelp.app.ui.screens.tasks.TasksScreen
import com.strangerhelp.app.ui.screens.meets.MeetsScreen
import com.strangerhelp.app.ui.screens.wallet.WalletScreen
import com.strangerhelp.app.ui.screens.leaderboard.LeaderboardScreen
import com.strangerhelp.app.ui.screens.ask.AskScreen
import com.strangerhelp.app.ui.screens.pulse.PulseScreen
import com.strangerhelp.app.ui.screens.notifications.NotificationsScreen
import com.strangerhelp.app.ui.theme.Saffron
import com.strangerhelp.app.ui.theme.OnSaffron

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val iconOutlined: ImageVector) {
    object Feed : Screen("feed", "Feed", Icons.Filled.ViewStream, Icons.Outlined.ViewStream)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.Assignment, Icons.Outlined.Assignment)
    object Post : Screen("post", "Post", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline)
    object Chat : Screen("chat", "Chat", Icons.Filled.Chat, Icons.Outlined.Chat)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(Screen.Feed, Screen.Tasks, Screen.Post, Screen.Chat, Screen.Profile)

@Composable
fun AppNavigation(user: User, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    val snackbarHostState = remember { SnackbarHostState() }
    
    var unreadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while(true) {
            try {
                val res = com.strangerhelp.app.data.api.ApiClient.api.getNotifications()
                if (res.isSuccessful) {
                    unreadCount = res.body()?.unreadCount ?: 0
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
                                selectedIconColor = OnSaffron,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Saffron,
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Screen.Feed.route, Modifier.padding(padding)) {
            composable(Screen.Feed.route) { FeedScreen(navController, user) }
            composable(Screen.Tasks.route) { TasksScreen(navController) }
            composable(Screen.Post.route) { PostTaskScreen(navController) }
            composable(Screen.Chat.route) { ChatListScreen(navController, user) }
            composable(Screen.Profile.route) { ProfileScreen(navController, onLogout = onLogout) }
            
            composable("meets") { MeetsScreen(navController) }
            composable("wallet") { WalletScreen(navController) }
            composable("leaderboard") { LeaderboardScreen(navController) }
            composable("ask") { AskScreen(navController) }
            composable("pulse") { PulseScreen(navController) }
            composable("notifications") { NotificationsScreen(navController) }
            composable("postMeet") { PostMeetScreen(navController) }
            composable("postQuestion") { PostQuestionScreen(navController) }
            composable("edit_profile") { EditProfileScreen(navController, user) }
            
            composable(
                "task/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { entry ->
                TaskDetailScreen(navController, user, entry.arguments?.getString("taskId") ?: "")
            }
            
            composable(
                "chat/{convId}",
                arguments = listOf(navArgument("convId") { type = NavType.StringType })
            ) { entry ->
                ChatDetailScreen(navController, user, entry.arguments?.getString("convId") ?: "")
            }
        }
    }
    }
}
