package com.strangerhelp.app.navigation
import com.strangerhelp.app.ui.screens.WebViewScreen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.strangerhelp.app.ui.screens.notifications.NotificationViewModel
import com.strangerhelp.app.ui.screens.notifications.NotificationViewModelFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navDeepLink
import androidx.navigation.navArgument
import com.strangerhelp.app.data.model.User
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strangerhelp.app.ui.screens.chat.ChatViewModel
import com.strangerhelp.app.ui.screens.chat.ChatDetailScreen
import com.strangerhelp.app.ui.screens.legal.LegalScreen
import com.strangerhelp.app.ui.screens.legal.LegalTexts
import com.strangerhelp.app.ui.screens.chat.ChatListScreen
import com.strangerhelp.app.ui.screens.ask.AskListScreen
import com.strangerhelp.app.ui.screens.ask.AskPostScreen
import com.strangerhelp.app.ui.screens.ask.AskDetailScreen
import com.strangerhelp.app.ui.screens.feed.FeedScreen
import com.strangerhelp.app.ui.screens.profile.VerifyIdScreen
import com.strangerhelp.app.ui.screens.profile.ReferEarnScreen
import com.strangerhelp.app.ui.screens.profile.KarmaWalletScreen

import com.strangerhelp.app.ui.screens.meets.CreateMeetScreen
import com.strangerhelp.app.ui.screens.meets.MeetDetailScreen
import com.strangerhelp.app.ui.screens.meets.MeetViewModel
import com.strangerhelp.app.ui.screens.meets.MeetViewModelFactory
import com.strangerhelp.app.ui.screens.meets.MeetsListScreen
import com.strangerhelp.app.data.repository.MeetRepository


import com.strangerhelp.app.ui.screens.profile.VerificationScreen
import com.strangerhelp.app.ui.screens.profile.AuthViewModel

import com.strangerhelp.app.ui.screens.post.PostTaskScreen
import com.strangerhelp.app.ui.screens.post.PostMeetScreen
import com.strangerhelp.app.ui.screens.post.PostQuestionScreen
import com.strangerhelp.app.ui.screens.profile.ProfileScreen
import com.strangerhelp.app.ui.screens.profile.EditProfileScreen
import com.strangerhelp.app.ui.screens.tasks.TaskDetailScreen
import com.strangerhelp.app.ui.screens.tasks.TasksScreen
import com.strangerhelp.app.ui.screens.wallet.WalletScreen
import com.strangerhelp.app.ui.screens.leaderboard.LeaderboardScreen
import com.strangerhelp.app.ui.screens.pulse.PulseScreen
import com.strangerhelp.app.ui.screens.notifications.NotificationsScreen
import com.strangerhelp.app.ui.screens.path.PathSetupScreen
import com.strangerhelp.app.ui.screens.path.PathActiveScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val iconOutlined: ImageVector) {
    object Feed : Screen("feed", "Home", Icons.Filled.DynamicFeed, Icons.Outlined.DynamicFeed)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.Assignment, Icons.Outlined.Assignment)
    object Post : Screen("post", "Post", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline)
    object Chat : Screen("chat", "Chat", Icons.Filled.Chat, Icons.Outlined.Chat)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(Screen.Feed, Screen.Tasks, Screen.Post, Screen.Chat, Screen.Profile)

@Composable
fun AppNavigation(user: User, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()

    val meetViewModel: MeetViewModel = viewModel(
        factory = MeetViewModelFactory(
            MeetRepository(com.strangerhelp.app.data.api.ApiClient.api),
            com.strangerhelp.app.data.repository.AuthRepository(com.strangerhelp.app.data.api.ApiClient.api)
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Feed.route
    // Show bottom bar on all feature pages (feed, tasks, post, chat, profile, pulse/live, path, meets, ask, wallet, leaderboard, etc.)
    // Only hide on full screen camera and webview
    val isFullScreenCameraOrWeb = currentRoute.startsWith("gps_camera") || currentRoute.startsWith("webview")
    val showBottomBar = !isFullScreenCameraOrWeb
    val snackbarHostState = remember { SnackbarHostState() }
    
    val notificationViewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory())
    val profileViewModel: com.strangerhelp.app.ui.screens.profile.ProfileViewModel = viewModel()
    val liveUser by profileViewModel.user.collectAsStateWithLifecycle()
    val activeUser = liveUser ?: user
    val askViewModel: com.strangerhelp.app.ui.screens.ask.AskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.strangerhelp.app.ui.screens.ask.AskViewModelFactory(
            com.strangerhelp.app.data.repository.AskRepository(com.strangerhelp.app.data.api.ApiClient.api),
            com.strangerhelp.app.data.repository.AuthRepository(com.strangerhelp.app.data.api.ApiClient.api)
        )
    )

    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        notificationViewModel.startPolling()
    }

    androidx.compose.runtime.CompositionLocalProvider(com.strangerhelp.app.ui.components.LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF6F6F6))) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { screen ->
                                val selected = when (screen) {
                                    Screen.Feed -> currentRoute == Screen.Feed.route
                                    Screen.Tasks -> currentRoute == Screen.Tasks.route ||
                                                    currentRoute.startsWith("tasks?") ||
                                                    currentRoute.startsWith("my_tasks")
                                    Screen.Post -> currentRoute == Screen.Post.route ||
                                                   currentRoute == "postMeet" ||
                                                   currentRoute == "postQuestion"
                                    Screen.Chat -> currentRoute == Screen.Chat.route ||
                                                   currentRoute.startsWith("chat/") ||
                                                   currentRoute == "support"
                                    Screen.Profile -> currentRoute == Screen.Profile.route ||
                                                      currentRoute.startsWith("profile/") ||
                                                      currentRoute == "edit_profile" ||
                                                      currentRoute == "verify_id" ||
                                                      currentRoute == "refer_earn" ||
                                                      currentRoute == "karma_wallet" ||
                                                      currentRoute == "wallet"
                                }
                                CustomBottomNavItem(
                                    screen = screen,
                                    selected = selected,
                                    unreadCount = if (screen == Screen.Chat) unreadCount else 0,
                                    onClick = {
                                        if (screen == Screen.Feed) {
                                            // Pressing Home from ANY screen navigates reliably back to Feed
                                            if (currentRoute != Screen.Feed.route) {
                                                navController.navigate(Screen.Feed.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        inclusive = false
                                                        saveState = false
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = false
                                                }
                                            }
                                        } else {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(navController, startDestination = Screen.Feed.route, Modifier.padding(padding)) {
                composable(Screen.Feed.route) { FeedScreen(navController, activeUser) }
                composable(Screen.Tasks.route) { TasksScreen(navController) }
                composable(
                    route = "tasks?category={category}",
                    arguments = listOf(navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category")
                    TasksScreen(navController, initialCategory = category)
                }
                composable(
                    route = "my_tasks?filter={filter}",
                    arguments = listOf(navArgument("filter") { type = NavType.StringType; defaultValue = "all" })
                ) { backStackEntry ->
                    val filter = backStackEntry.arguments?.getString("filter") ?: "all"
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val myTasksViewModel: com.strangerhelp.app.ui.screens.tasks.MyTasksViewModel = viewModel(
                        factory = com.strangerhelp.app.ui.screens.tasks.MyTasksViewModelFactory(
                            com.strangerhelp.app.data.repository.TaskRepository(com.strangerhelp.app.data.api.ApiClient.api)
                        )
                    )
                    com.strangerhelp.app.ui.screens.tasks.MyTasksScreen(
                        initialFilter = filter,
                        navController = navController,
                        viewModel = myTasksViewModel,
                        currentUserId = activeUser?.id
                    )
                }
                composable(Screen.Post.route) { PostTaskScreen(navController) }
                composable(Screen.Chat.route) { ChatListScreen(viewModel = chatViewModel, navController = navController) }
                composable("support") { com.strangerhelp.app.ui.screens.chat.SupportChatScreen(navController = navController) }
                composable(Screen.Profile.route) { ProfileScreen(navController, onLogout = onLogout, viewModel = profileViewModel) }
                
                // removed MeetsScreen
                composable("wallet") { WalletScreen(navController) }
                composable("leaderboard") { LeaderboardScreen(navController) }
                
                composable("pulse") { PulseScreen(navController) }

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

                composable("notifications") { NotificationsScreen(navController) }
                composable(
                    "webview?url={url}",
                    arguments = listOf(navArgument("url") { type = NavType.StringType })
                ) { entry ->
                    val url = entry.arguments?.getString("url") ?: ""
                    WebViewScreen(url = url, navController = navController)
                }
                composable("disclaimer") {
                    LegalScreen(
                        title = "Disclaimer",
                        content = LegalTexts.DISCLAIMER,
                        navController = navController
                    )
                }
                composable("cookie_policy") {
                    LegalScreen(
                        title = "Cookie Policy",
                        content = LegalTexts.COOKIE_POLICY,
                        navController = navController
                    )
                }
                composable("community_guidelines") {
                    LegalScreen(
                        title = "Community Guidelines",
                        content = LegalTexts.COMMUNITY_GUIDELINES,
                        navController = navController
                    )
                }


                composable("meets") { com.strangerhelp.app.ui.screens.meets.MeetsListScreen(meetViewModel, navController) }
                composable("create_meet") { com.strangerhelp.app.ui.screens.meets.CreateMeetScreen(meetViewModel, navController) }
                composable(
                    route = "meet_detail/{meetId}",
                    arguments = listOf(navArgument("meetId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val meetId = backStackEntry.arguments?.getString("meetId") ?: ""
                    com.strangerhelp.app.ui.screens.meets.MeetDetailScreen(meetId = meetId, viewModel = meetViewModel, navController = navController)
                }
                composable("profile/{userId}") { ProfileScreen(navController, onLogout = onLogout) }
                composable("path_setup") { PathSetupScreen(navController) }
                composable("path_active") { PathSetupScreen(navController) }

                composable("postMeet") { PostMeetScreen(navController) }
                composable("postQuestion") { PostQuestionScreen(navController) }
                composable("edit_profile") { EditProfileScreen(navController, activeUser, viewModel = profileViewModel) }

                composable("verify_id") {
                    VerifyIdScreen(navController = navController)
                }
                
                composable("refer_earn") {
                    ReferEarnScreen(navController = navController)
                }
                
                composable("karma_wallet") {
                    KarmaWalletScreen(navController = navController)
                }

                composable(
                    "verification?token={token}",
                    arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true; defaultValue = "" }),
                    deepLinks = listOf(navDeepLink { uriPattern = "https://strangerhelp.com/api/auth/verify-email?token={token}" })
                ) { backStackEntry ->
                    val token = backStackEntry.arguments?.getString("token") ?: ""
                    VerificationScreen(
                        token = token,
                        navController = navController
                    )
                }
                
                composable(
                    "gps_camera/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId") ?: ""
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    val gpsCameraViewModel: com.strangerhelp.app.ui.screens.tasks.GpsCameraViewModel = 
                        androidx.lifecycle.viewmodel.compose.viewModel(factory = com.strangerhelp.app.ui.screens.tasks.GpsCameraViewModelFactory(context))

                    com.strangerhelp.app.ui.screens.tasks.GpsCameraScreen(
                        taskId = taskId,
                        onSubmitProof = { bytes, onResult ->
                            gpsCameraViewModel.submitProof(taskId, bytes) { success ->
                                if (success) {
                                    onResult(true, null)
                                    navController.popBackStack()
                                } else {
                                    onResult(false, gpsCameraViewModel.error.value ?: "Failed to submit proof. Please try again.")
                                }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

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
                    ChatDetailScreen(conversationId = entry.arguments?.getString("convId") ?: "", viewModel = chatViewModel, navController = navController)
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    screen: Screen,
    selected: Boolean,
    unreadCount: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val bgColor = if (selected) Color(0xFFFFB340) else Color.Transparent
    val contentColor = if (selected) Color(0xFF1F1F1F) else Color(0xFF333333)
    val fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(64.dp)
            .clip(CircleShape)
            .background(bgColor)
            .testTag("footer_${screen.label.lowercase()}")
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Or use a custom ripple if desired
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (unreadCount > 0) {
                BadgedBox(badge = { Badge { Text(unreadCount.toString()) } }) {
                    Icon(
                        imageVector = if (selected) screen.icon else screen.iconOutlined,
                        contentDescription = screen.label,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = if (selected) screen.icon else screen.iconOutlined,
                    contentDescription = screen.label,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = screen.label,
                fontSize = 12.sp,
                fontWeight = fontWeight,
                color = contentColor
            )
        }
    }
}
