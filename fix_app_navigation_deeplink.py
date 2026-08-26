import re

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace('import androidx.navigation.compose.*', 'import androidx.navigation.compose.*\nimport androidx.navigation.navDeepLink')

target_composable = """                composable(
                    "verification?token={token}",
                    arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true; defaultValue = "" })
                ) {"""

replacement_composable = """                composable(
                    "verification?token={token}",
                    arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true; defaultValue = "" }),
                    deepLinks = listOf(navDeepLink { uriPattern = "https://strangerhelp.com/api/auth/verify-email?token={token}" })
                ) {"""

content = content.replace(target_composable, replacement_composable)

with open("app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
