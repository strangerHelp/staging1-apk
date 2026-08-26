import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

imports = """import com.strangerhelp.app.ui.screens.profile.VerificationScreen
import com.strangerhelp.app.ui.screens.profile.AuthViewModel
"""

content = content.replace("import com.strangerhelp.app.ui.screens.feed.FeedScreen", "import com.strangerhelp.app.ui.screens.feed.FeedScreen\n" + imports)

# Find composable(Screen.Profile.route) { ProfileScreen(...) } and update the route for VerificationScreen
verification_route = """
                composable(
                    "verification?token={token}",
                    arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true; defaultValue = "" })
                ) { backStackEntry ->
                    val token = backStackEntry.arguments?.getString("token") ?: ""
                    VerificationScreen(
                        token = token,
                        navController = navController
                    )
                }"""

if "verification?token" not in content:
    content = content.replace('composable("edit_profile") { EditProfileScreen(navController, user) }', 'composable("edit_profile") { EditProfileScreen(navController, user) }' + verification_route)

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
